package com.example.sparks

import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.here.android.mpa.common.*
import com.here.android.mpa.mapping.AndroidXMapFragment
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapGesture
import com.here.android.mpa.mapping.MapMarker
import com.here.android.mpa.mapping.MapRoute
import com.here.android.mpa.routing.*
import kotlinx.android.synthetic.main.activity_parking.*
import kotlinx.android.synthetic.main.dialog_logs.view.*
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

class SelectParkingActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

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

    private var  lastPos: MapMarker?= null
    private var currPos: GeoCoordinate? = null
    private var currRoute: MapRoute? = null
    private var map: Map? = null
    private var mapFragment: AndroidXMapFragment? = null
    private lateinit var posManager: PositioningManager
    private lateinit var router: CoreRouter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parking)
        var toolbar = findViewById<Toolbar>(R.id.drawer_toolbar)
        setSupportActionBar(toolbar)

        var drawer: DrawerLayout = findViewById(R.id.drawer_layout)

        var naviationView: NavigationView = findViewById(R.id.nav_view_drawer)

        naviationView.setNavigationItemSelectedListener(this)

        var toggle: ActionBarDrawerToggle = ActionBarDrawerToggle(this, drawer, toolbar,
            R.string.navigation_bar_open, R.string.navigation_bar_close)

        drawer.addDrawerListener(toggle)

        toggle.syncState()

        periodTextView.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setTitle("Izaberite period rezervacije")
            val dialogLayout = inflater.inflate(R.layout.dialog_extend, null)
            builder.setView(dialogLayout)
            builder.setPositiveButton("Ok"){ dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }
        group1.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setTitle("Izaberite parking")
            val dialogLayout = inflater.inflate(R.layout.dialog_parking, null)
            builder.setView(dialogLayout)
            builder.setPositiveButton("Ok"){ dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

        routeParking.setOnClickListener { showRoute() }

        initialize()

    }

    private fun routeListenerFactory(): CoreRouter.Listener{
        return object: CoreRouter.Listener{
            override fun onCalculateRouteFinished(p0: MutableList<RouteResult>?, p1: RoutingError) {
                if(p1 == RoutingError.NONE){

                    if(currRoute != null)
                        map!!.removeMapObject(currRoute!!)

                    currRoute = MapRoute(p0!![0].route)

                    map!!.addMapObject(currRoute!!)
                }
            }

            override fun onProgress(p0: Int) {}

        }

    }

    private fun markerListenerFactory(): MapGesture.OnGestureListener{
        return object: MapGesture.OnGestureListener{

            override fun onLongPressRelease() {}

            override fun onRotateEvent(p0: Float): Boolean {
                return false
            }

            override fun onMultiFingerManipulationStart() {}

            override fun onPinchLocked() {}

            override fun onPinchZoomEvent(p0: Float, p1: PointF): Boolean {
                return false
            }

            override fun onTapEvent(p0: PointF): Boolean {
                if(lastPos != null)
                    map!!.removeMapObject(lastPos!!)

                val image = Image()
                image.setImageResource(R.drawable.parking_pin_large)

                lastPos = MapMarker(map!!.pixelToGeo(p0)!!, image)
                map?.addMapObject(lastPos!!)

                return true
            }

            override fun onPanStart() {}

            override fun onMultiFingerManipulationEnd() {}

            override fun onDoubleTapEvent(p0: PointF): Boolean {
                return false
            }

            override fun onPanEnd() {}

            override fun onTiltEvent(p0: Float): Boolean {
                return false
            }

            override fun onMapObjectsSelected(p0: MutableList<ViewObject>): Boolean {
                return false
            }

            override fun onRotateLocked() {}

            override fun onLongPressEvent(p0: PointF): Boolean {
                return false
            }

            override fun onTwoFingerTapEvent(p0: PointF): Boolean {
                return false
            }
        }

    }

    private fun showRoute() {
        if(lastPos != null){
            val coordinate: GeoCoordinate = lastPos!!.coordinate

            val routePlan = RoutePlan()

            routePlan.addWaypoint(RouteWaypoint(currPos!!))

            routePlan.addWaypoint(RouteWaypoint(coordinate))

            val routeOptions = RouteOptions()

            routeOptions.transportMode = RouteOptions.TransportMode.CAR
            routeOptions.routeType = RouteOptions.Type.BALANCED

            routePlan.routeOptions = routeOptions

            router.calculateRoute(routePlan, routeListenerFactory())
        }
    }

    private fun initialize() {

        mapFragment = supportFragmentManager.findFragmentById(R.id.map_view_main) as AndroidXMapFragment?

        var success:Boolean = MapSettings.setIsolatedDiskCacheRootPath(
            applicationContext.getExternalFilesDir(null)!!.absolutePath + File.separator + ".here-maps"
        )

        if(!success)
            Toast.makeText(applicationContext, "Unable to set isolated disk cache path",
                Toast.LENGTH_LONG).show()
       // else{
            mapFragment?.init{
                if(it == OnEngineInitListener.Error.NONE){
                    map = mapFragment!!.map
                    map!!.setCenter(GeoCoordinate(49.196261, -123.004773, 0.0), Map.Animation.NONE)
                    posManager = PositioningManager.getInstance()
                    posManager.start(PositioningManager.LocationMethod.GPS_NETWORK)

                    // Define positioning listener
                    // Define positioning listener

                    val positionListener: PositioningManager.OnPositionChangedListener =
                        object : PositioningManager.OnPositionChangedListener {
                            override fun onPositionUpdated(
                                method: PositioningManager.LocationMethod,
                                position: GeoPosition?, isMapMatched: Boolean
                            ) {
                                if (position != null) {
                                    map!!.setCenter(
                                        position.coordinate,
                                        Map.Animation.NONE

                                    )

                                    mapFragment!!.positionIndicator!!.isVisible = true

                                    /*Toast.makeText(applicationContext, "Pozicija " + String.format(
                                        Locale.US, "%.6f, %.6f", position.coordinate.longitude, position.coordinate.latitude)
                                        , Toast.LENGTH_LONG).show()*/
                                }

                                currPos = position!!.coordinate



                            }

                            override fun onPositionFixChanged(
                                method: PositioningManager.LocationMethod,
                                status: PositioningManager.LocationStatus
                            ) {
                            }
                        }

                    // Register positioning listener

                    posManager.addListener(
                        WeakReference(positionListener)
                    )
                    map!!.setZoomLevel((map!!.maxZoomLevel + map!!.minZoomLevel) / 2)


                    mapFragment!!.mapGesture!!.addOnGestureListener(markerListenerFactory(), 1, false)

                    router = CoreRouter()

                } else
                    Toast.makeText(applicationContext, "Cannot Initialize Map Fragment" + it.details,
                        Toast.LENGTH_LONG).show()

            }
        //}


    }

}