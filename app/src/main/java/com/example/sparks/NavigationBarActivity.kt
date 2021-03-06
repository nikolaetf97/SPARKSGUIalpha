package com.example.sparks

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.dialog_logs.view.*
import java.util.*

abstract class NavigationBarActivity(private var itemId: Int = 0) : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

 protected lateinit var drawer: DrawerLayout

 protected fun setup(
  layoutID: Int,
  fab: FloatingActionButton?)
 {
  val sharedPref = getSharedPreferences("preferences", Context.MODE_PRIVATE)
  val lang = sharedPref.getString("LANG","sr")
  val locale = Locale(lang!!)

  Locale.setDefault(locale)

  val config = Configuration()
  config.locale = locale
  baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

  setContentView(layoutID)

  val toolbar = findViewById<Toolbar>(R.id.drawer_toolbar)
  setSupportActionBar(toolbar)
  setTheme(sharedPref.getInt("THEME",R.style.AppTheme))
  toolbar.setBackgroundColor(sharedPref.getInt("BACKGROUND",resources.getColor(R.color.colorPrimary)))

  drawer = findViewById(R.id.drawer_layout)
  val naviationView: NavigationView = findViewById(R.id.nav_view_drawer)

  naviationView.setNavigationItemSelectedListener(this)

  val toggle = ActionBarDrawerToggle(this, drawer, toolbar,
   R.string.navigation_bar_open,
   R.string.navigation_bar_close)

  drawer.addDrawerListener(toggle)
  toggle.syncState()


  fab?.backgroundTintList = ColorStateList.valueOf(sharedPref
   .getInt("BACKGROUND",
    resources.getColor(R.color.colorPrimary)))
 }

 override fun onNavigationItemSelected(p0: MenuItem): Boolean {
  if (p0.itemId != itemId) {
   when (p0.itemId) {

    R.id.nav_plates -> startActivity(Intent(this, PlatesActivity::class.java))

    R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))

    R.id.nav_home -> startActivity(Intent(this, MainActivity::class.java))

    R.id.report_error -> {
     val builder = AlertDialog.Builder(this)
     val inflater = layoutInflater

     builder.setTitle(getString(R.string.error_info_value))

     val dialogLayout = inflater.inflate(R.layout.error_dialog, null)
     val editText  = dialogLayout.findViewById<EditText>(R.id.editText)

     builder.setView(dialogLayout)
     builder.setPositiveButton("OK"){ _, _ ->
      val emailIntent = Intent(Intent.ACTION_SEND)

      emailIntent.type = "text/plain"
      emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("spark-feedback@outlook.com"))
      emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback")
      emailIntent.putExtra(Intent.EXTRA_TEXT, editText.text)
      emailIntent.type = "message/rfc822"

      startActivity(Intent.createChooser(emailIntent, "Send email using..."))
     }
     builder.show()}

    R.id.nav_logs -> {
     val logsLayout = layoutInflater.inflate(R.layout.dialog_logs, null)
     val logsDialog = AlertDialog.Builder(this)

     logsDialog.setView(logsLayout)
     logsDialog.setTitle(getString(R.string.logovi))

     logsLayout.recycler_view.layoutManager = LinearLayoutManager(this)
     logsLayout.recycler_view.adapter = LogDataAdapter(this, LogDataSupplier.logData)

     logsDialog.setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
     logsDialog.show()
     return false }}

   drawer.closeDrawer(GravityCompat.START, false)

    return true
  } else
    return false
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