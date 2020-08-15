package com.example.sparks

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.util.*


class LogsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        if(p0.itemId != itemId) {
            when (p0.itemId) {
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))

                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))

                R.id.nav_logs -> startActivity(Intent(this, LogsActivity::class.java))

                R.id.nav_home -> startActivity(Intent(this, MainActivity::class.java))

                R.id.nav_plates -> startActivity(Intent(this, PlatesActivity::class.java))

                R.id.user_manual -> {
                    val alert= AlertDialog.Builder(this)
                    alert.setTitle(getString(R.string.how_to_use))
                    alert.setMessage(getString(R.string.how_to_use_value))
                    alert.setPositiveButton("OK"){dialog, which ->
                        dialog.dismiss()
                    }
                    alert.show()
                }
                R.id.report_error ->{
                    val builder = AlertDialog.Builder(this)
                    val inflater = layoutInflater
                    builder.setTitle(getString(R.string.error_info_value))
                    val dialogLayout = inflater.inflate(R.layout.error_dialog, null)
                    val editText  = dialogLayout.findViewById<EditText>(R.id.editText)
                    builder.setView(dialogLayout)
                    builder.setPositiveButton("OK"){dialog, which ->
                        val emailIntent = Intent(Intent.ACTION_SEND)
                        emailIntent.setType("text/plain")
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("spark-feedback@outlook.com"))
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback")
                        emailIntent.putExtra(Intent.EXTRA_TEXT, editText.text)
                        emailIntent.setType("message/rfc822")
                        startActivity(Intent.createChooser(emailIntent, "Send email using..."))
                    }
                    builder.show()
                }

                R.id.about -> {
                    var alert=AlertDialog.Builder(this)
                    alert.setTitle(getString(R.string.about_app))
                    alert.setMessage(getString(R.string.about_app_value))
                    alert.setPositiveButton("OK"){dialog,which->
                        dialog.dismiss()
                    }
                    alert.show()
                }
            }
            return true
        } else
            return false

    }

    var isOpen = false
    var Marko=false

    private lateinit var drawer: DrawerLayout
    private val itemId = R.id.nav_logs

    override fun onRestart() {
        super.onRestart()
        val sharedPref = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val lang = sharedPref.getString("LANG","sr")
        val locale = Locale(lang!!)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        val toolbar = findViewById<Toolbar>(R.id.drawer_toolbar)

        setTheme(sharedPref.getInt("THEME",R.style.AppTheme))
        toolbar.setBackgroundColor(sharedPref.getInt("BACKGROUND",resources.getColor(R.color.colorPrimary)))
        recreate()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val lang = sharedPref.getString("LANG","sr")
        val locale = Locale(lang!!)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var toolbar = findViewById<Toolbar>(R.id.drawer_toolbar)
        setSupportActionBar(toolbar)

        setTheme(sharedPref.getInt("THEME",R.style.AppTheme))
        toolbar.setBackgroundColor(sharedPref.getInt("BACKGROUND",resources.getColor(R.color.colorPrimary)))

        drawer = findViewById(R.id.drawer_layout)

        var naviationView: NavigationView = findViewById(R.id.nav_view_drawer)

        naviationView.setNavigationItemSelectedListener(this)

        var toggle: ActionBarDrawerToggle = ActionBarDrawerToggle(this, drawer, toolbar,
            R.string.navigation_bar_open, R.string.navigation_bar_close)

        drawer.addDrawerListener(toggle)

        toggle.syncState()

    }

    override fun onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
