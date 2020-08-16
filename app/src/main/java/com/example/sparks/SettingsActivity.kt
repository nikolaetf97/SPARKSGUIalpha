package com.example.sparks

import android.os.Bundle

class SettingsActivity : NavigationBarActivity(R.id.nav_settings) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup(R.layout.activity_settings, null)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()

    }
}