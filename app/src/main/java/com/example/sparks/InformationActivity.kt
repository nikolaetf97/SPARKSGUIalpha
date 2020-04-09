package com.example.sparks

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.dialog_logs.view.*

class InformationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
            when (p0.itemId) {
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))

                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))

                R.id.nav_home -> startActivity(Intent(this, MainActivity::class.java))

                R.id.user_manual -> {
                    val alert= AlertDialog.Builder(this)
                    alert.setTitle("Uputstvo za upotrebu")
                    alert.setMessage("1.Kao korisnik ove aplikacije potrebno je prvo da odaberete destinaciju, kako bi vam aplikacija mogla prikazati informacije o parking mjestima u krugu od 500 " +
                            "2.odaberite parking, potom je potebno da unesete registarske tablice, vrijeme na koje se parking placa \n" +
                            "3.nakon toga pritisnite posalji")
                    alert.setPositiveButton("OK"){dialog, which ->
                        dialog.dismiss()
                    }
                    alert.show()
                }
                R.id.report_error ->{
                    val builder = AlertDialog.Builder(this)
                    val inflater = layoutInflater
                    builder.setTitle("Ako ste primjetili ikakve greške u radu aplikacije, molimo vas da ih ukratko opišete")
                    val dialogLayout = inflater.inflate(R.layout.error_dialog, null)
                    val editText  = dialogLayout.findViewById<EditText>(R.id.editText)
                    builder.setView(dialogLayout)
                    builder.setPositiveButton("OK"){dialog, which ->
                        dialog.dismiss()
                    }
                    builder.show()
                }

                R.id.about -> {
                    var alert= AlertDialog.Builder(this)
                    alert.setTitle("O aplikaciji")
                    alert.setMessage("Aplikacija SPARK je rezultat urađenog projekta iz predmeta Projektovanje Softvera, omogućava korisniku da lakše pronađe slobodno parking mjesto kao i da mu prikaže mnoštvo korisnih informacija o parkinzima u Banja Luci \n \nVerzija:1.0")
                    alert.setPositiveButton("OK"){dialog,which->
                        dialog.dismiss()
                    }
                    alert.show()
                }

                R.id.nav_logs -> {
                    val logsLayout = layoutInflater.inflate(R.layout.dialog_logs, null)
                    val logsDialog = AlertDialog.Builder(this)
                    logsDialog.setView(logsLayout)
                    logsDialog.setTitle("Logovi")
                    logsLayout.recycler_view.layoutManager = LinearLayoutManager(this)
                    logsLayout.recycler_view.adapter= LogDataAdapter(this, Supplier.logData)
                    logsDialog.setPositiveButton("Ok"){ dialog, _ ->dialog.dismiss() }
                    logsDialog.show()
                    return false
                }
            }
            return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        var toolbar = findViewById<Toolbar>(R.id.drawer_toolbar)
        setSupportActionBar(toolbar)

        var drawer: DrawerLayout = findViewById(R.id.drawer_layout)

        var naviationView: NavigationView = findViewById(R.id.nav_view_drawer)

        naviationView.setNavigationItemSelectedListener(this)

        var toggle: ActionBarDrawerToggle = ActionBarDrawerToggle(this, drawer, toolbar,
            R.string.navigation_bar_open, R.string.navigation_bar_close)

        drawer.addDrawerListener(toggle)

        toggle.syncState()

        findViewById<TextView>(R.id.textView3).setOnClickListener{
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setTitle("Izaberite period skraćenja")
            val dialogLayout = inflater.inflate(R.layout.dialog_extend, null)
            builder.setView(dialogLayout)
            builder.setPositiveButton("Ok"){ dialog, _ ->
                    dialog.dismiss()
            }
            builder.show()
        }

    }
}