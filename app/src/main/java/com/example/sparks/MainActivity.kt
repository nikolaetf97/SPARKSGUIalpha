package com.example.sparks

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PointF
import android.os.Bundle
import android.os.Handler
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.doAfterTextChanged
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.here.android.mpa.common.*
import com.here.android.mpa.mapping.*
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.routing.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.fab
import kotlinx.android.synthetic.main.activity_main.registryNumberEditText
import java.lang.ref.WeakReference
import java.util.*

class MainActivity : NavigationBarActivity(R.id.nav_home) {

    companion object {
        var DESTINATION: MapMarker? = null
    }
    private var backPressed: Long = 0
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
    private lateinit var notificationManager: NotificationManagerCompat


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup(R.layout.activity_main, fab)

        val sharedPref = getSharedPreferences("preferences", Context.MODE_PRIVATE)


        notificationManager = NotificationManagerCompat.from(this)
        periodTextView.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setTitle(getString(R.string.parking_duration))
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

        fab.backgroundTintList = ColorStateList.valueOf(
            sharedPref.getInt("BACKGROUND",
                resources.getColor(R.color.colorPrimary)))

        swipeContainer.setOnRefreshListener {
            Handler().postDelayed({
                swipeContainer.isRefreshing = false

                when {
                    DESTINATION == null -> Toast.makeText(this, "Niste izabrali parking", Toast.LENGTH_LONG).show()
                    currPos == null -> Toast.makeText(this, "Molimo sačekajte", Toast.LENGTH_LONG).show()
                    else -> showRoute()
                }
            }
                ,((Random().nextInt(4) + 1) * 1000).toLong()) }

        swipeContainer.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        initialize()
        initGetPSpotsWorker()
    }

    private fun routerListenerFactory(): CoreRouter.Listener {
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

    private fun gestureListenerFactory(): MapGesture.OnGestureListener {
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
                        if (spots.isNotEmpty()) {
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

            router.calculateRoute(routePlan, routerListenerFactory())
        }
    }

    private fun initialize() {

        mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapfragment) as AndroidXMapFragment?

        mapFragment?.init {
            if (it == OnEngineInitListener.Error.NONE) {
                map = mapFragment!!.map
                map!!.setCenter(GeoCoordinate(44.771926, 17.208906, 0.0), Map.Animation.NONE)
                posManager = PositioningManager.getInstance()

                val positionListener: PositioningManager.OnPositionChangedListener =
                    object : PositioningManager.OnPositionChangedListener {
                        override fun onPositionUpdated(
                            method: PositioningManager.LocationMethod,
                            position: GeoPosition?, isMapMatched: Boolean
                        ) {
                            if (position != null) {

                                mapFragment!!.positionIndicator!!.isVisible = true
                                currPos = position.coordinate
                            }

                        }

                        override fun onPositionFixChanged(
                            method: PositioningManager.LocationMethod,
                            status: PositioningManager.LocationStatus
                        ) {
                        }
                    }

                posManager.addListener(
                    WeakReference(positionListener)
                )

                posManager.start(PositioningManager.LocationMethod.GPS_NETWORK)

                map!!.setCenter(
                    posManager.position.coordinate,
                    Map.Animation.NONE

                )

                map!!.zoomLevel = (map!!.maxZoomLevel + map!!.minZoomLevel) / 2
                mapFragment!!.mapGesture!!.addOnGestureListener(gestureListenerFactory(), 1, false)
                router = CoreRouter()

                PSpotSupplier.init(applicationContext)

                map!!.addMapObjects(PSpotSupplier.parkingSports.map { ps -> ps.getMarker() })
                PSpotSupplier.addMap(map!!)

            } else
                Toast.makeText(
                    applicationContext, "Cannot Initialize Map Fragment" + it.details,
                    Toast.LENGTH_LONG
                ).show()

        }
    }

    /*private fun addNewPosListener() {
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
    }*/

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

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            val exitToast = Toast.makeText(baseContext, getString(R.string.exit_msg), Toast.LENGTH_SHORT)
            if (backPressed + 2000 > System.currentTimeMillis()){

                finishAffinity()
            }
            else{
                exitToast.show()
            }
            backPressed = System.currentTimeMillis()
        }
    }
}
