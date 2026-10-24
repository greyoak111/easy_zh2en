/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 easy_zh2en contributors
 */
package org.fcitx.fcitx5.android.translate

import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.input.FcitxInputMethodService

/**
 * The toolbar's translate button.
 * Tap: translate the selection, or (with nothing selected) the text before the cursor, and replace it in place.
 * Long-press: put the original back, as long as the translated text is still right there.
 */
class TranslateAction(private val service: FcitxInputMethodService) {

    private val prefs get() = AppPrefs.getInstance().translate

    private var job: Job? = null
    private var lastOriginal: String? = null
    private var lastTranslation: String? = null

    private fun toast(msg: String, long: Boolean = false) =
        Toast.makeText(service, msg, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()

    fun onClick(button: View) {
        if (job?.isActive == true) return
        if (prefs.apiKey.getValue().isBlank()) { toast(service.getString(R.string.translate_no_key), true); return }
        val ic = service.currentInputConnection ?: return
        val selected = ic.getSelectedText(0)?.toString().orEmpty()
        val source = selected.ifEmpty { ic.getTextBeforeCursor(MAX_CHARS, 0)?.toString().orEmpty() }
        if (source.isBlank()) { toast(service.getString(R.string.translate_nothing)); return }
        val replaceSelection = selected.isNotEmpty()
        button.alpha = 0.4f
        toast(service.getString(R.string.translate_working))
        job = service.lifecycleScope.launch {
            try {
                val r = Translator.translate(source, prefs)
                val ic2 = service.currentInputConnection ?: return@launch
                ic2.beginBatchEdit()
                try {
                    if (replaceSelection) {
                        // only replace if the same text is still selected
                        if (ic2.getSelectedText(0)?.toString() != source) return@launch
                    } else {
                        // only replace if the text before the cursor hasn't changed while we waited
                        if (ic2.getTextBeforeCursor(source.length, 0)?.toString() != source) return@launch
                        ic2.deleteSurroundingText(source.length, 0)
                    }
                    ic2.commitText(r.translation, 1)
                } finally {
                    ic2.endBatchEdit()
                }
                lastOriginal = source
                lastTranslation = r.translation
                if (r.back.isNotEmpty()) toast(service.getString(R.string.translate_back, r.back), true)
            } catch (e: Exception) {
                toast(service.getString(R.string.translate_failed, e.message ?: e.javaClass.simpleName), true)
            } finally {
                button.alpha = 1f
            }
        }
    }

    fun onLongClick(): Boolean {
        val orig = lastOriginal
        val tr = lastTranslation
        val ic = service.currentInputConnection
        if (orig == null || tr == null || ic == null || ic.getTextBeforeCursor(tr.length, 0)?.toString() != tr) {
            toast(service.getString(R.string.translate_nothing_to_undo))
            return true
        }
        ic.beginBatchEdit()
        ic.deleteSurroundingText(tr.length, 0)
        ic.commitText(orig, 1)
        ic.endBatchEdit()
        lastOriginal = null
        lastTranslation = null
        toast(service.getString(R.string.translate_undone))
        return true
    }

    companion object {
        /** how much text before the cursor to take when nothing is selected */
        const val MAX_CHARS = 2000
    }
}
