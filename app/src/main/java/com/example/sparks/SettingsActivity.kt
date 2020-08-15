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
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.dialog_logs.view.*
import java.util.*

class SettingsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        if(p0.itemId != itemId) {
            when (p0.itemId) {
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))

                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))

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
                        dialog.dismiss()
                    }
                    builder.show()
                }

                R.id.about -> {
                    var alert= AlertDialog.Builder(this)
                    alert.setTitle(getString(R.string.about_app))
                    alert.setMessage(getString(R.string.about_app_value))
                    alert.setPositiveButton("OK"){dialog,which->
                        dialog.dismiss()
                    }
                    alert.show()
                }

                R.id.nav_logs -> {
                    val logsLayout = layoutInflater.inflate(R.layout.dialog_logs, null)
                    val logsDialog = AlertDialog.Builder(this)
                    logsDialog.setView(logsLayout)
                    logsDialog.setTitle(getString(R.string.logovi))
                    logsLayout.recycler_view.layoutManager = LinearLayoutManager(this)
                    logsLayout.recycler_view.adapter= LogDataAdapter(this, Supplier.logData)
                    logsDialog.setPositiveButton("Ok"){ dialog, _ ->dialog.dismiss() }
                    logsDialog.show()
                    return false
                }
            }
            return true
        } else
            return false
    }

    private val itemId = R.id.nav_settings

    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPref = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val lang = sharedPref.getString("LANG","sr")
        val locale = Locale(lang!!)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        var toolbar = findViewById<Toolbar>(R.id.drawer_toolbar)
        setSupportActionBar(toolbar)

        setTheme(sharedPref.getInt("THEME",R.style.AppTheme))
        toolbar.setBackgroundColor(sharedPref.getInt("BACKGROUND",resources.getColor(R.color.colorPrimary)))


        var drawer: DrawerLayout = findViewById(R.id.drawer_layout)

        var naviationView: NavigationView = findViewById(R.id.nav_view_drawer)

        naviationView.setNavigationItemSelectedListener(this)

        var toggle: ActionBarDrawerToggle = ActionBarDrawerToggle(this, drawer, toolbar,
            R.string.navigation_bar_open, R.string.navigation_bar_close)

        drawer.addDrawerListener(toggle)

        toggle.syncState()



        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()

    }

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
}