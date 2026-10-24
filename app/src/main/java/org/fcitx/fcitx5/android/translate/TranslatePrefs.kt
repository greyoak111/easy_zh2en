/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 easy_zh2en contributors
 */
package org.fcitx.fcitx5.android.translate

import android.content.Context
import android.content.SharedPreferences
import android.text.InputType
import androidx.annotation.StringRes
import androidx.preference.EditTextPreference
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.prefs.ManagedPreference
import org.fcitx.fcitx5.android.data.prefs.ManagedPreferenceCategory
import org.fcitx.fcitx5.android.data.prefs.ManagedPreferenceUi

/** Settings → Translate. Everything stays in the IME's own SharedPreferences; the key is only ever sent to [baseUrl]. */
class TranslatePrefs(sharedPreferences: SharedPreferences) :
    ManagedPreferenceCategory(R.string.translate, sharedPreferences) {

    val baseUrl = text(R.string.translate_base_url, "translate_base_url", "https://api.deepseek.com")
    val apiKey = text(R.string.translate_api_key, "translate_api_key", "", secret = true)
    val model = text(R.string.translate_model, "translate_model", "deepseek-chat")
    val target = text(R.string.translate_target, "translate_target", "English")

    val tone = list(
        R.string.translate_tone,
        "translate_tone",
        "casual",
        object : ManagedPreference.StringLikeCodec<String> {
            override fun decode(raw: String): String = raw
        },
        listOf("casual", "technical", "formal"),
        listOf(R.string.translate_tone_casual, R.string.translate_tone_technical, R.string.translate_tone_formal)
    )

    private fun text(@StringRes title: Int, key: String, defaultValue: String, secret: Boolean = false): ManagedPreference.PString {
        val pref = ManagedPreference.PString(sharedPreferences, key, defaultValue)
        pref.register()
        TextUi(title, key, defaultValue, secret).registerUi()
        return pref
    }

    class TextUi(
        @StringRes val title: Int,
        key: String,
        val defaultValue: String,
        val secret: Boolean
    ) : ManagedPreferenceUi<EditTextPreference>(key) {
        override fun createUi(context: Context) = EditTextPreference(context).apply {
            key = this@TextUi.key
            isIconSpaceReserved = false
            isSingleLineTitle = false
            setDefaultValue(defaultValue)
            setTitle(this@TextUi.title)
            setDialogTitle(this@TextUi.title)
            setOnBindEditTextListener {
                it.isSingleLine = true
                if (secret) it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            summaryProvider = androidx.preference.Preference.SummaryProvider<EditTextPreference> { p ->
                val v = p.text.orEmpty()
                when {
                    !secret -> v
                    v.isEmpty() -> context.getString(R.string.translate_key_unset)
                    else -> context.getString(R.string.translate_key_set, v.takeLast(4))
                }
            }
        }
    }
}
