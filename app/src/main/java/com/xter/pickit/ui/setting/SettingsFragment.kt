package com.xter.pickit.ui.setting

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.xter.pickit.R
import com.xter.pickit.ext.CONFIG


class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = CONFIG;
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}