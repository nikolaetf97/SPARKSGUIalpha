package com.example.sparks

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.widget.RemoteViews
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.makeText
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.here.android.mpa.common.GeoCoordinate

class SPARKService : Service() {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null

    private val checkArrivalCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            val currentLocation: Location = locationResult.lastLocation
            val gCoord = GeoCoordinate(currentLocation.latitude, currentLocation.longitude, currentLocation.altitude)

            makeText(applicationContext, gCoord.toString(), LENGTH_LONG).show()
            makeText(applicationContext, "Udaljenost " + gCoord.distanceTo(MainActivity.DESTINATION!!.coordinate).toString(),
                LENGTH_LONG).show()

            if(gCoord.distanceTo(MainActivity.DESTINATION!!.coordinate) < 11){
                arrivalTime = System.currentTimeMillis()
                cost++
                prepareCheckLeavingNotification()
                startLocationUpdates(checkLeavingCallback)
                stopLocationUpdates(this)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        initData()
    }

    val checkLeavingCallback: LocationCallback = object : LocationCallback() {

        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)

            val currentLocation = p0!!.lastLocation
            val gCoord = GeoCoordinate(currentLocation.latitude, currentLocation.longitude, currentLocation.altitude)

            if(gCoord.distanceTo(MainActivity.DESTINATION!!.coordinate) < 11){
                enteredParking = true
            } else{
                if(enteredParking){
                    leavingTime = System.currentTimeMillis()
                    enteredParking = false
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        prepareForegroundNotification()
        context = this
        startLocationUpdates(checkArrivalCallback)
        return START_STICKY
    }

    fun stopLocationUpdates(callback: LocationCallback){
        mFusedLocationClient!!.removeLocationUpdates(callback)
    }

    fun startLocationUpdates(callback: LocationCallback) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mFusedLocationClient!!.requestLocationUpdates(
            locationRequest,
            callback, Looper.myLooper()
        )
    }

    fun prepareForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                App.channelID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            App.LOCATION_SERVICE_REQUEST,
            notificationIntent, 0
        )
        val notification =
            NotificationCompat.Builder(this, App.channelID)
                .setContentTitle("SPARK Location Service")
                .setSmallIcon(R.drawable.parking_button_foreground)
                .setContentIntent(pendingIntent)
                .build()
        startForeground(App.LOCATION_SERVICE, notification)
    }

    fun prepareCheckLeavingNotification(){
        val extendIntent = Intent(this, ExtendBroadcastReceiver::class.java)
        val showInfoIntent = Intent(this, ShowInfoIntentService::class.java)

        extendIntent.action = "EXTEND_DEFAULT"
        showInfoIntent.action= "SHOW_INFO"
        val goBackToMainIntent = PendingIntent.getActivity(this, 0,
            Intent(this, MainActivity::class.java), 0)

        val notificationViewsExpanded = RemoteViews(packageName, R.layout.notification_layout_expanded)
        val notificationViewsCollapsed = RemoteViews(packageName, R.layout.notification_layout_collapsed)

        notificationViewsExpanded.setOnClickPendingIntent(R.id.bt_extend, PendingIntent.getBroadcast(
            this, 0, extendIntent, PendingIntent.FLAG_UPDATE_CURRENT
        ))
        notificationViewsExpanded.setOnClickPendingIntent(R.id.bt_info, PendingIntent.getService(
            this, 0, showInfoIntent, PendingIntent.FLAG_UPDATE_CURRENT
        ))

        val notificationBuilder = NotificationCompat.Builder(this, "channel1")
            .setSmallIcon(R.drawable.parking_pin_large)
            .setContentTitle(getString(R.string.remaining_time))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setCustomContentView(notificationViewsCollapsed)
            .setCustomBigContentView(notificationViewsExpanded)
            .setContentIntent(goBackToMainIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)

        App.notificationManager!!.notify(App.LOCATION_SERVICE, notificationBuilder.build())
        ParkingCountdownTimer.init(MainActivity.length!!,
            App.LOCATION_SERVICE,
            packageName,
            notificationBuilder, this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun initData() {
        locationRequest = LocationRequest.create()
        locationRequest!!.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(MainActivity.context)
    }

    companion object {
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 3000
        lateinit var context: Context
        private var enteredParking = false
        var leavingTime: Long = 0
        var arrivalTime: Long = 0
        var cost: Int = 0
    }
}