package com.example.sparks

import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.os.Handler
import android.telephony.SmsManager
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.doAfterTextChanged
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.navigation.NavigationView
import com.here.android.mpa.common.*
import com.here.android.mpa.mapping.*
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.routing.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.fab
import kotlinx.android.synthetic.main.activity_main.registryNumberEditText
import kotlinx.android.synthetic.main.activity_parking.*
import kotlinx.android.synthetic.main.dialog_logs.view.*
import java.lang.ref.WeakReference
import java.util.*


/*
*
* TODO("Jednostavije bi bilo da na MainActivity se unesu tablice itd.. da se ne komplikuje sa 2 mape")
*
* */

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        if (p0.itemId != itemId) {
            when (p0.itemId) {
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))

                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))

                R.id.nav_home -> startActivity(Intent(this, MainActivity::class.java))

                R.id.user_manual -> {
                    val alert = AlertDialog.Builder(this)
                    alert.setTitle("Uputstvo za upotrebu")
                    alert.setMessage(
                        "1.Kao korisnik ove aplikacije potrebno je prvo da odaberete destinaciju, kako bi vam aplikacija mogla prikazati informacije o parking mjestima u krugu od 500 " +
                                "2.odaberite parking, potom je potebno da unesete registarske tablice, vrijeme na koje se parking placa \n" +
                                "3.nakon toga pritisnite posalji"
                    )
                    alert.setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    alert.show()
                }
                R.id.report_error -> {
                    val builder = AlertDialog.Builder(this)
                    val inflater = layoutInflater
                    builder.setTitle("Ako ste primjetili ikakve greške u radu aplikacije, molimo vas da ih ukratko opišete")
                    val dialogLayout = inflater.inflate(R.layout.error_dialog, null)
                    dialogLayout.findViewById<EditText>(R.id.editText)
                    builder.setView(dialogLayout)
                    builder.setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                }

                R.id.about -> {
                    val alert = AlertDialog.Builder(this)
                    alert.setTitle("O aplikaciji")
                    alert.setMessage("Aplikacija SPARK je rezultat urađenog projekta iz predmeta Projektovanje Softvera, omogućava korisniku da lakše pronađe slobodno parking mjesto kao i da mu prikaže mnoštvo korisnih informacija o parkinzima u Banja Luci \n \nVerzija:1.0")
                    alert.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    alert.show()
                }

                R.id.nav_logs -> {
                    val logsLayout = layoutInflater.inflate(R.layout.dialog_logs, null)
                    val logsDialog = AlertDialog.Builder(this)
                    logsDialog.setView(logsLayout)
                    logsDialog.setTitle("Logovi")
                    logsLayout.recycler_view.layoutManager = LinearLayoutManager(this)
                    logsLayout.recycler_view.adapter = LogDataAdapter(this, Supplier.logData)
                    logsDialog.setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
                    logsDialog.show()
                    return false
                }
            }
            return true
        } else
            return false

    }

    companion object {
        var DESTINATION: MapMarker? = null
    }

    private var destinationSelected: Boolean = false
    private var platesSelected: Boolean = false
    private var periodSelected: Boolean = false
    private var pastOverlay: MapOverlay? = null

    private var lastPos: MapMarker? = null
    private var currPos: GeoCoordinate? = null
    private var currRoute: MapRoute? = null
    private var map: Map? = null
    private var mapFragment: AndroidXMapFragment? = null
    private lateinit var posManager: PositioningManager
    private lateinit var router: CoreRouter
    private var parkingListener: PositioningManager.OnPositionChangedListener? = null

    private lateinit var drawer: DrawerLayout
    private lateinit var notificationManager: NotificationManagerCompat

    /*private lateinit var mRandom: Random
    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable*/
    private val itemId = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.drawer_toolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        val naviationView: NavigationView = findViewById(R.id.nav_view_drawer)
        naviationView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar,
            R.string.navigation_bar_open, R.string.navigation_bar_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        notificationManager = NotificationManagerCompat.from(this)
        periodTextView.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setTitle("Izaberite period rezervacije")
            val dialogLayout = inflater.inflate(R.layout.dialog_reserve, null)

            dialogLayout.findViewById<RadioGroup>(R.id.period_radio_group)
                .setOnCheckedChangeListener{ _: RadioGroup, i: Int ->
                    when(i){
                        R.id.extend1 -> {
                            SelectParkingActivity.length = 5*60*1000
                            periodTextView.text = "Period: 5min"
                        }

                        R.id.extend2 -> {
                            SelectParkingActivity.length = 10*60*1000
                            periodTextView.text = "Period: 10min"
                        }

                        R.id.extend3 -> {
                            SelectParkingActivity.length = 15*60*1000
                            periodTextView.text = "Period: 15min"
                        }

                        R.id.extend4 -> {
                            SelectParkingActivity.length = 20*60*1000
                            periodTextView.text = "Period: 20min"
                        }

                        R.id.extend5 -> {
                            SelectParkingActivity.length = 30*60*1000
                            periodTextView.text = "Period: 30min"
                        }
                    }

                    if(i != -1){
                        periodSelected = true
                        fab.isClickable = periodSelected && platesSelected && destinationSelected
                    }
                }

            registryNumberEditText.doAfterTextChanged {
                platesSelected = !it.isNullOrBlank()
                fab.isClickable = periodSelected && platesSelected && destinationSelected

                if(platesSelected)
                    SelectParkingActivity.plates = it.toString()
            }

            builder.setView(dialogLayout)
            builder.setPositiveButton("Ok"){ dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

        fab.setOnClickListener {
            WorkManager
                .getInstance(applicationContext)
                .enqueueUniqueWork(CheckArrivalWorker.TAG, ExistingWorkPolicy.KEEP,
                    OneTimeWorkRequestBuilder<CheckArrivalWorker>().build())
        }

        swipeContainer.setOnRefreshListener {
            Handler().postDelayed({
                swipeContainer.isRefreshing = false
                showRoute()}
                ,((Random().nextInt(4) + 1) * 1000).toLong())
        }

        swipeContainer.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        /*
        mHandler = Handler()
        mRandom = Random()

        swipeContainer.setOnRefreshListener {

            mRunnable = Runnable {
                swipeContainer.isRefreshing = false
                ShowPreferableSpotsTask(map!!, lastPos!!.coordinate)

                //showRoute()
                /*val quickExtendIntent = Intent(this, QuickExtendBroadcastReceiver::class.java)

                val goBackToAppPending = PendingIntent.getActivity(this, 0,
                    Intent(this, MainActivity::class.java), 0)

                val notificationViews = RemoteViews(packageName, R.layout.notification_layout)

                notificationViews.setOnClickPendingIntent(R.id.quickExtendTimer, PendingIntent.getActivity(
                    this, 0, quickExtendIntent, 0
                ))

                val notificationBuilder = NotificationCompat.Builder(this, "channel1")
                    .setSmallIcon(R.drawable.parking_pin_large)
                    .setContentTitle("Preostalo vrijeme na parkingu")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                    .setCustomContentView(RemoteViews(
                        packageName, R.layout.notification_layout
                    ))
                    .setContentIntent(goBackToAppPending)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)

                val notification = notificationBuilder.build()

                notificationManager.notify(1, notification)*/


            }

            mHandler.postDelayed(mRunnable, ((Random().nextInt(4) + 1) * 1000).toLong())
        }
        swipeContainer.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
*/

        initialize()
        initGetPSpotsWorker()
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun routeListenerFactory(): CoreRouter.Listener {
        return object : CoreRouter.Listener {
            override fun onCalculateRouteFinished(p0: MutableList<RouteResult>?, p1: RoutingError) {
                if (p1 == RoutingError.NONE) {

                    if (currRoute != null)
                        map!!.removeMapObject(currRoute!!)

                    currRoute = MapRoute(p0!![0].route)
                    map!!.addMapObject(currRoute!!)
                }
            }

            override fun onProgress(p0: Int) {}
        }
    }

    private fun markerListenerFactory(): MapGesture.OnGestureListener {
        return object : MapGesture.OnGestureListener {

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
                if (pastOverlay != null)
                    map!!.removeMapOverlay(pastOverlay!!)

                pastOverlay = null

                return true
            }

            override fun onPanStart() {}

            override fun onMultiFingerManipulationEnd() {}

            /*
            *
            * Na dvoklik se postavlja marker, jer
            * na jedan klik na marker se dobija info o njemu
            *
            * */

            override fun onDoubleTapEvent(p0: PointF): Boolean {
                if (lastPos != null)
                    map!!.removeMapObject(lastPos!!)

                val image = Image()
                image.setImageResource(R.drawable.parking_pin_large)

                lastPos = MapMarker(map!!.pixelToGeo(p0)!!, image)
                map?.addMapObject(lastPos!!)

                PSpotSupplier.showNearbyMarkers(3, lastPos!!.coordinate)

                if(currRoute != null)
                    map!!.removeMapObject(currRoute!!)

                if (DESTINATION != null){
                    PSpotSupplier.parkingSports
                        .filter { spot -> spot.getMarker() == DESTINATION }[0]
                        .shrinkMarker()
                    DESTINATION = null
                    destinationSelected = false
                    fab.isClickable = periodSelected && platesSelected && destinationSelected
                }

                return true
            }

            override fun onPanEnd() {}

            override fun onTiltEvent(p0: Float): Boolean {
                return false
            }

            override fun onMapObjectsSelected(p0: MutableList<ViewObject>): Boolean {
                for (viewObject in p0) {
                    if ((viewObject as MapObject).type == MapObject.Type.MARKER && (viewObject as MapMarker) != lastPos) {
                        if (pastOverlay != null)
                            map!!.removeMapOverlay(pastOverlay!!)

                        val spots =
                            PSpotSupplier.parkingSports.filter { spot -> spot.getMarker() == viewObject }
                        if (!spots.isEmpty()) {
                            val spot = spots[0]

                            val view = layoutInflater.inflate(R.layout.infobubble, null)

                            val icon = view.findViewById<ImageView>(R.id.icon_parking)
                            icon.setImageResource(viewObject.description!!.toInt())

                            val ocupation = view.findViewById<TextView>(R.id.tv_ocupation)
                            val placeholder =
                                spot.freeSpace.toString() + "/" + spot.space.toString()
                            ocupation.text = placeholder

                            val name = view.findViewById<TextView>(R.id.tv_name)
                            name.text = spot.name

                            val zone = view.findViewById<TextView>(R.id.tv_zone)
                            zone.text = "1"

                            val button = view.findViewById<Button>(R.id.tv_select)

                            button.setOnClickListener {
                                val tmpMarker = viewObject
                                if (DESTINATION != null)
                                    PSpotSupplier.parkingSports
                                        .filter { spot -> spot.getMarker() == DESTINATION }[0]
                                        .shrinkMarker()

                                DESTINATION = tmpMarker
                                destinationSelected = true
                                fab.isClickable = periodSelected && platesSelected && destinationSelected

                                PSpotSupplier.parkingSports
                                    .filter { spot -> spot.getMarker() == DESTINATION }[0]
                                    .expandMarker()

                                if(pastOverlay != null)
                                    map!!.removeMapOverlay(pastOverlay!!)
                                if(currRoute != null)
                                    map!!.removeMapObject(currRoute!!)
                            }
                            val overlay = MapOverlay(view, viewObject.coordinate)

                            map!!.addMapOverlay(overlay)

                            pastOverlay = overlay

                            break
                        }
                    }
                }
                return true
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
        if (lastPos != null) {
            val coordinate: GeoCoordinate = DESTINATION!!.coordinate

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

        mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapfragment) as AndroidXMapFragment?

        /*val success: Boolean = MapSettings.setIsolatedDiskCacheRootPath(
            applicationContext.getExternalFilesDir(null)!!.absolutePath + File.separator + ".here-maps"
        )

        if(!success)
            Toast.makeText(applicationContext, "Unable to set isolated disk cache path",
                Toast.LENGTH_LONG).show()
        else{*/
        mapFragment?.init {
            if (it == OnEngineInitListener.Error.NONE) {
                map = mapFragment!!.map
                map!!.setCenter(GeoCoordinate(49.196261, -123.004773, 0.0), Map.Animation.NONE)
                posManager = PositioningManager.getInstance()
                posManager.start(PositioningManager.LocationMethod.GPS_NETWORK)

                // Define positioning listener

                val positionListener: PositioningManager.OnPositionChangedListener =
                    object : PositioningManager.OnPositionChangedListener {
                        override fun onPositionUpdated(
                            method: PositioningManager.LocationMethod,
                            position: GeoPosition?, isMapMatched: Boolean
                        ) {
                            if (position != null) {

                                mapFragment!!.positionIndicator!!.isVisible = true

                                /*Toast.makeText(applicationContext, "Pozicija " + String.format(
                                        Locale.US, "%.6f, %.6f", position.coordinate.longitude, position.coordinate.latitude)
                                        , Toast.LENGTH_LONG).show()*/
                                currPos = position!!.coordinate
                            }

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

                map!!.setCenter(
                    posManager.position.coordinate,
                    Map.Animation.NONE

                )

                map!!.zoomLevel = (map!!.maxZoomLevel + map!!.minZoomLevel) / 2
                posManager.position
                mapFragment!!.mapGesture!!.addOnGestureListener(markerListenerFactory(), 1, false)
                router = CoreRouter()

                PSpotSupplier.init()

                map!!.addMapObjects(PSpotSupplier.parkingSports.map { ps -> ps.getMarker() })
                PSpotSupplier.addMap(map!!)

            } else
                Toast.makeText(
                    applicationContext, "Cannot Initialize Map Fragment" + it.details,
                    Toast.LENGTH_LONG
                ).show()

        }
        //}
    }

    private fun addNewPosListener() {
        if (parkingListener == null) {
            parkingListener =
                object : PositioningManager.OnPositionChangedListener {
                    override fun onPositionUpdated(
                        method: PositioningManager.LocationMethod,
                        position: GeoPosition?, isMapMatched: Boolean
                    ) {
                        Toast.makeText(
                            applicationContext, "Udaljenost: " + String.format(
                                Locale.US, "%.6f",
                                position!!.coordinate.distanceTo(lastPos!!.coordinate)
                            ), Toast.LENGTH_LONG
                        ).show()

                        if (position.coordinate.distanceTo(lastPos!!.coordinate) < 30) {
                            val smsManager: SmsManager = SmsManager.getDefault()

                            smsManager.sendTextMessage(
                                "+38765185060",
                                null, "Uspjeh",
                                null, null
                            )
                        }
                    }

                    override fun onPositionFixChanged(
                        method: PositioningManager.LocationMethod,
                        status: PositioningManager.LocationStatus
                    ) {
                    }
                }

            posManager.addListener(
                WeakReference(parkingListener)
            )
        }
    }

    private fun initGetPSpotsWorker(){
        val getPSpotsWorker = OneTimeWorkRequestBuilder<GetPSpotsWorker>()
            .build()
        val processPSpotsWorker = OneTimeWorkRequestBuilder<ProcessPSpotsWorker>()
            .build()

        WorkManager
            .getInstance()
            .beginWith(getPSpotsWorker)
            .then(processPSpotsWorker)
            .enqueue()

    }
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
