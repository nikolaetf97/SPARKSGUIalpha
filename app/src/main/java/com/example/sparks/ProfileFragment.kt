package com.example.sparks

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class ProfileFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_profile, rootKey)
    }
}