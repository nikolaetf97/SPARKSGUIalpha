package com.example.sparks

import android.os.Bundle

class ProfileActivity : NavigationBarActivity(R.id.nav_profile) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup(R.layout.activity_profile, null)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.profile, ProfileFragment())
            .commit()

    }
}