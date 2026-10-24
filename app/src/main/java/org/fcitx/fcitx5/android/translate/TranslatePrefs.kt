/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 easy_zh2en contributors
 */
package org.fcitx.fcitx5.android.translate

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.text.InputType
import android.widget.EditText
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.prefs.ManagedPreference
import org.fcitx.fcitx5.android.data.prefs.ManagedPreferenceCategory
import org.fcitx.fcitx5.android.data.prefs.ManagedPreferenceUi

/**
 * Settings → Translate. Everything stays in the IME's own SharedPreferences; the key is only ever sent to [baseUrl].
 * [baseUrl] and [model] always hold the values actually used: picking a provider fills them in (see [TranslateSettingsFragment]),
 * and the URL is only editable for "custom".
 */
class TranslatePrefs(sharedPreferences: SharedPreferences) :
    ManagedPreferenceCategory(R.string.translate, sharedPreferences) {

    private val stringCodec = object : ManagedPreference.StringLikeCodec<String> {
        override fun decode(raw: String): String = raw
    }

    val provider = list(
        R.string.translate_provider,
        KEY_PROVIDER,
        "deepseek",
        stringCodec,
        Providers.all.map { it.id },
        Providers.all.map { it.label }
    )

    val baseUrl = text(R.string.translate_base_url, KEY_BASE_URL, "https://api.deepseek.com") {
        provider.getValue() == Providers.CUSTOM
    }

    val model: ManagedPreference.PString = ManagedPreference.PString(sharedPreferences, KEY_MODEL, "deepseek-chat").also {
        it.register()
        ModelUi(KEY_MODEL, "deepseek-chat") { Providers.get(provider.getValue()).models }.registerUi()
    }

    val apiKey = text(R.string.translate_api_key, "translate_api_key", "", secret = true)

    val target = list(
        R.string.translate_target,
        "translate_target",
        "English",
        stringCodec,
        Providers.targets.map { it.first },
        Providers.targets.map { it.second }
    )

    val tone = list(
        R.string.translate_tone,
        "translate_tone",
        "casual",
        stringCodec,
        listOf("casual", "technical", "formal"),
        listOf(R.string.translate_tone_casual, R.string.translate_tone_technical, R.string.translate_tone_formal)
    )

    private fun text(
        @StringRes title: Int,
        key: String,
        defaultValue: String,
        secret: Boolean = false,
        enableUiOn: (() -> Boolean)? = null
    ): ManagedPreference.PString {
        val pref = ManagedPreference.PString(sharedPreferences, key, defaultValue)
        pref.register()
        TextUi(title, key, defaultValue, secret, enableUiOn).registerUi()
        return pref
    }

    class TextUi(
        @StringRes val title: Int,
        key: String,
        val defaultValue: String,
        val secret: Boolean,
        enableUiOn: (() -> Boolean)? = null
    ) : ManagedPreferenceUi<EditTextPreference>(key, enableUiOn) {
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
            summaryProvider = Preference.SummaryProvider<EditTextPreference> { p ->
                val v = p.text.orEmpty()
                when {
                    !secret -> v
                    v.isEmpty() -> context.getString(R.string.translate_key_unset)
                    else -> context.getString(R.string.translate_key_set, v.takeLast(4))
                }
            }
        }
    }

    /** Model dropdown: the current provider's models, plus "custom…" to type any model name. */
    class ModelUi(
        key: String,
        val defaultValue: String,
        val presets: () -> List<String>
    ) : ManagedPreferenceUi<ListPreference>(key) {
        override fun createUi(context: Context) = ModelPreference(context, presets).apply {
            key = this@ModelUi.key
            isIconSpaceReserved = false
            isSingleLineTitle = false
            setDefaultValue(defaultValue)
            setTitle(R.string.translate_model)
            setDialogTitle(R.string.translate_model)
            // entries are filled right before the dialog opens; give it something non-null until then
            entries = arrayOf(defaultValue)
            entryValues = arrayOf(defaultValue)
            summaryProvider = Preference.SummaryProvider<ListPreference> { it.value.orEmpty() }
        }
    }

    class ModelPreference(context: Context, private val presets: () -> List<String>) : ListPreference(context) {

        init {
            setOnPreferenceChangeListener { _, newValue ->
                if (newValue != CUSTOM_MODEL) return@setOnPreferenceChangeListener true
                askCustom()
                false
            }
        }

        override fun onClick() {
            val list = presets().toMutableList()
            value?.takeIf { it.isNotEmpty() && it !in list }?.let { list.add(it) }
            entries = (list + context.getString(R.string.translate_model_custom)).toTypedArray()
            entryValues = (list + CUSTOM_MODEL).toTypedArray()
            super.onClick()
        }

        private fun askCustom() {
            val input = EditText(context).apply {
                isSingleLine = true
                setText(value)
                setSelection(text.length)
            }
            val pad = (20 * context.resources.displayMetrics.density).toInt()
            val box = FrameLayout(context).apply { setPadding(pad, pad / 2, pad, 0); addView(input) }
            AlertDialog.Builder(context)
                .setTitle(R.string.translate_model)
                .setView(box)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    input.text.toString().trim().takeIf { it.isNotEmpty() }?.let { value = it }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    companion object {
        const val KEY_PROVIDER = "translate_provider"
        const val KEY_BASE_URL = "translate_base_url"
        const val KEY_MODEL = "translate_model"
        private const val CUSTOM_MODEL = "\u0000custom"
    }
}
