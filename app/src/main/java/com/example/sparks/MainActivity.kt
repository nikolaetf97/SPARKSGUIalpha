package com.example.sparks

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.here.android.mpa.common.*
import com.here.android.mpa.mapping.*
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.routing.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.io.PrintWriter
import java.lang.ref.WeakReference
import java.util.*

class MainActivity : NavigationBarActivity(R.id.nav_home) {

    companion object {
        lateinit var server_ip: String
        var DESTINATION: MapMarker? = null
        var posManager: PositioningManager? = null
        var plates: String? = null
        var length: Long? = null
        lateinit var context: Context
    }
    private lateinit var platesList : ArrayList<PlatesData>

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

    private lateinit var router: CoreRouter
    private lateinit var notificationManager: NotificationManagerCompat


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setup(R.layout.activity_main, fab)

        val sharedPref = getSharedPreferences("preferences", Context.MODE_PRIVATE)

        server_ip = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("server_ip", "def")!!
        Log.d("maintag", server_ip)

        loadData()
        context = applicationContext

        val platesName = ArrayList<String>()
        platesList.forEach { platesName.add(it.name) }

        val arrayAdapter = ArrayAdapter<String>(this,R.layout.style_spinner,platesName)
        val spinner = findViewById<Spinner>(R.id.registryNumberEditText)
        spinner.adapter = arrayAdapter

        val index = sharedPref.getInt("INDEX",-1)
        if(index != -1) {
            spinner.setSelection(index)
        }
       spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
           override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
               plates = spinner.selectedItem.toString()
               with(sharedPref?.edit()){
                   this?.putInt("INDEX", spinner.selectedItemPosition)
                   this?.commit()
               }
               platesSelected = true
               fab.isClickable = periodSelected && platesSelected && destinationSelected
           }

           override fun onNothingSelected(p0: AdapterView<*>?) { platesSelected = false }
       }

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
                            length = 60000
                            periodTextView.text = "Period: 1min"
                        }

                        R.id.extend2 -> {
                            length = 120000
                            periodTextView.text = "Period: 2min"
                        }

                        R.id.extend3 -> {
                            length = 180000
                            periodTextView.text = "Period: 3min"
                        }

                        R.id.extend4 -> {
                            length = 240000
                            periodTextView.text = "Period: 4min"
                        }

                        R.id.extend5 -> {
                            length = 300000
                            periodTextView.text = "Period: 5min"
                        }
                    }

                    if(i != -1){
                        periodSelected = true
                        fab.isClickable = periodSelected && platesSelected && destinationSelected
                    }
                }

            builder.setView(dialogLayout)
            builder.setPositiveButton("Ok"){ dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

        fab.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, SPARKService::class.java))
            } else{
                startService(Intent(this, SPARKService::class.java))
            }
        }

        fab.backgroundTintList = ColorStateList.valueOf(
            sharedPref.getInt("BACKGROUND",
                resources.getColor(R.color.colorPrimary)))

        swipeContainer.setOnRefreshListener {
            Handler().postDelayed({
                swipeContainer.isRefreshing = false

                when {
                    DESTINATION == null -> Toast.makeText(this, getString(R.string.no_parking), Toast.LENGTH_LONG).show()
                    currPos == null -> Toast.makeText(this, getString(R.string.wait), Toast.LENGTH_LONG).show()
                    else -> showRoute()
                }
            },
                ((Random().nextInt(4) + 1) * 1000).toLong()) }

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
                    PSpotSupplier.parkingSpots
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

                        val spots = PSpotSupplier.parkingSpots.filter { spot -> spot.getMarker() == viewObject }
                        if (spots.isNotEmpty()) {
                            val spot = spots[0]

                            val view = layoutInflater.inflate(R.layout.infobubble, null)

                            val icon = view.findViewById<ImageView>(R.id.icon_parking)
                            icon.setImageResource(viewObject.description!!.toInt())

                            val ocupation = view.findViewById<TextView>(R.id.tv_len)
                            val placeholder =
                                spot.freeSpace.toString() + "/" + spot.space.toString()
                            ocupation.text = placeholder

                            val name = view.findViewById<TextView>(R.id.tv_loc)
                            name.text = spot.name

                            val zone = view.findViewById<TextView>(R.id.tv_cost)
                            zone.text = "1"

                            val button = view.findViewById<Button>(R.id.tv_select)

                            button.setOnClickListener {
                                val tmpMarker = viewObject
                                if (DESTINATION != null)
                                    PSpotSupplier.parkingSpots
                                        .filter { spot -> spot.getMarker() == DESTINATION }[0]
                                        .shrinkMarker()

                                DESTINATION = tmpMarker
                                destinationSelected = true
                                fab.isClickable = periodSelected && platesSelected && destinationSelected

                                PSpotSupplier.parkingSpots
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

        mapFragment = supportFragmentManager.findFragmentById(R.id.mapfragment) as AndroidXMapFragment?

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

                posManager!!.addListener(
                    WeakReference(positionListener)
                )

                posManager!!.start(PositioningManager.LocationMethod.GPS_NETWORK)

                map!!.setCenter(
                    posManager!!.position.coordinate,
                    Map.Animation.NONE

                )

                map!!.zoomLevel = (map!!.maxZoomLevel + map!!.minZoomLevel) / 2
                mapFragment!!.mapGesture!!.addOnGestureListener(gestureListenerFactory(), 1, false)
                router = CoreRouter()

                PSpotSupplier.init(applicationContext)

                map!!.addMapObjects(PSpotSupplier.parkingSpots.map { ps -> ps.getMarker() })
                PSpotSupplier.addMap(map!!)

            } else
                Toast.makeText(
                    applicationContext, getString(R.string.no_initialize) + it.details,
                    Toast.LENGTH_LONG
                ).show()

        }
    }

    //TODO("Dodati da se posalje poruka na +38765185060 kada se dodje na parking ili kada se produzi parking, u zavisnosti od tipa parkinga, i dana")

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

    override fun onDestroy() {
        var file = File(applicationContext.getExternalFilesDir(null)!!.path, "ps.pspots")
        PrintWriter(file.path).close()

        var tmp = Json{
            isLenient = true
        }.encodeToString(
            SetSerializer(PSpot.serializer()),
            PSpotSupplier.parkingSpots
        )

        file.writeText(tmp)
        file = File(applicationContext.getExternalFilesDir(null)!!.path, "ls.logs")
        PrintWriter(file.path).close()

        tmp = Json{
            isLenient = true
        }.encodeToString(
            ListSerializer(LogData.serializer()),
            LogDataSupplier.logData
        )

        file.writeText(tmp)
        super.onDestroy()
    }
    private fun loadData()
    {
        val sharedPreferences = getSharedPreferences("sharedP", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("dataList", null)

        val itemType = object : TypeToken<ArrayList<PlatesData>>() {}.type

        platesList = gson.fromJson<ArrayList<PlatesData>>(json, itemType) ?: ArrayList<PlatesData>()
    }
}