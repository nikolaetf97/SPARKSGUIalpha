package com.example.sparks

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.MapView
import com.google.android.material.navigation.NavigationView
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.mapviewlite.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_logs.view.*
import java.util.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        if(p0.itemId != itemId) {
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
                    val alert=AlertDialog.Builder(this)
                    alert.setTitle("O aplikaciji")
                    alert.setMessage("Aplikacija SPARK je rezultat urađenog projekta iz predmeta Projektovanje Softvera, omogućava korisniku da lakše pronađe slobodno parking mjesto kao i da mu prikaže mnoštvo korisnih informacija o parkinzima u Banja Luci \n \nVerzija:1.0")
                    alert.setPositiveButton("OK"){ dialog, _ -> dialog.dismiss() }
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
        } else
            return false

    }

    var isOpen = false
    var Marko=false
    private var mapView: MapViewLite? = null


    private lateinit var drawer: DrawerLayout
    private lateinit var mMapView: MapView
    private lateinit var mRandom: java.util.Random
    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable
    private lateinit var mapMarker: MapMarker
    private lateinit var geoCoordinates: GeoCoordinates
    private val itemId = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var toolbar = findViewById<Toolbar>(R.id.drawer_toolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)

        var naviationView: NavigationView = findViewById(R.id.nav_view_drawer)

        naviationView.setNavigationItemSelectedListener(this)

        var toggle: ActionBarDrawerToggle = ActionBarDrawerToggle(this, drawer, toolbar,
            R.string.navigation_bar_open, R.string.navigation_bar_close)

        drawer.addDrawerListener(toggle)

        toggle.syncState()


        var fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        var fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        var rClockWise = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise)
        var rCounterClockWise = AnimationUtils.loadAnimation(this, R.anim.rotate_counterclockwise)



        fab.setOnClickListener {
            startActivity(Intent(this, SelectParkingActivity::class.java))
        }

        fab.setOnLongClickListener {
            if (isOpen) {
                fab_edit_1.startAnimation(fabClose)
                fab_edit_2.startAnimation(fabClose)

                fab.startAnimation(rClockWise)
                isOpen = false

            } else {
                fab_edit_1.startAnimation(fabOpen)
                fab_edit_2.startAnimation(fabOpen)

                fab.startAnimation(rCounterClockWise)
                isOpen = true

                fab_edit_1.isClickable
                fab_edit_2.isClickable
            }
            true
        }

        fab_edit_1.setOnClickListener {
            startActivity(Intent(this, InformationActivity::class.java))
        }

        fab_edit_2.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setTitle("Izaberite period produženja")
            val dialogLayout = inflater.inflate(R.layout.dialog_extend, null)
            builder.setView(dialogLayout)
            builder.setPositiveButton("Ok"){ dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

        mHandler = Handler()
        mRandom = Random()

        swipeContainer.setOnRefreshListener {

            mRunnable = Runnable {
                swipeContainer.isRefreshing = false
            }

            mHandler.postDelayed(mRunnable, ((Random().nextInt(4) + 1) * 1000).toLong())
        }
        swipeContainer.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_red_dark
        )

        var mapViewVar: MapViewLite? = findViewById(R.id.map_view_main)


        mapViewVar?.onCreate(savedInstanceState)
        mapViewVar?.getMapScene()?.loadScene(
            MapStyle.NORMAL_DAY
        ) { errorCode ->
            if (errorCode == null) {
                mapViewVar.getCamera().target = GeoCoordinates(44.7722, 17.1910)
                mapViewVar.getCamera().zoomLevel = 16.0
            } else {
                Log.d(FragmentActivity.UI_MODE_SERVICE, "onLoadScene failed: $errorCode")
            }
        }
        var mapImage = MapImageFactory.fromResource(resources, R.drawable.parking_pin_large)

        mapMarker = MapMarker(GeoCoordinates(44.7690, 17.1885))

        var mapMarker2: MapMarker = MapMarker(GeoCoordinates(44.7682, 17.1855))

        var mapMarker3: MapMarker = MapMarker(GeoCoordinates(44.7676, 17.1893))

        mapMarker.addImage(mapImage, MapMarkerImageStyle())
        mapMarker2.addImage(mapImage, MapMarkerImageStyle())
        mapMarker3.addImage(mapImage, MapMarkerImageStyle())

        mapView = mapViewVar

        mapView?.mapScene?.addMapMarker(mapMarker)
        mapView?.mapScene?.addMapMarker(mapMarker3)
        mapView?.mapScene?.addMapMarker(mapMarker2)

    }

    override fun onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

   /* override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                this.startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }

            else -> super.onOptionsItemSelected(item)


        }
    }*/
}
