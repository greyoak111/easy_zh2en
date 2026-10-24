/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 easy_zh2en contributors
 */
package org.fcitx.fcitx5.android.translate

import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceScreen
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.data.prefs.ManagedPreferenceFragment

class TranslateSettingsFragment : ManagedPreferenceFragment(AppPrefs.getInstance().translate) {

    override fun onPreferenceUiCreated(screen: PreferenceScreen) {
        val url = screen.findPreference<EditTextPreference>(TranslatePrefs.KEY_BASE_URL) ?: return
        val model = screen.findPreference<ListPreference>(TranslatePrefs.KEY_MODEL) ?: return
        // picking a provider fills in its URL and default model; "custom" keeps whatever is there to edit from
        screen.findPreference<ListPreference>(TranslatePrefs.KEY_PROVIDER)?.setOnPreferenceChangeListener { _, v ->
            val p = Providers.get(v as String)
            if (p.id != Providers.CUSTOM) url.text = p.baseUrl
            p.models.firstOrNull()?.let { model.value = it }
            true
        }
    }
}
