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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.here.android.mpa.common.GeoCoordinate

/**
 * Starts location updates on background and publish LocationUpdateEvent upon
 * each new location result.
 */
class LocationUpdateService : Service() {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null

    //endregion
    //onCreate
    override fun onCreate() {
        super.onCreate()
        initData()
    }

    //Location Callback
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val currentLocation: Location = locationResult.lastLocation
            val gCoord = GeoCoordinate(currentLocation.latitude, currentLocation.longitude, currentLocation.altitude)
            Toast.makeText(context,
                gCoord.toString(),
                Toast.LENGTH_LONG).show()
            Toast.makeText(context,
                "Udaljenost " + gCoord.distanceTo(MainActivity.DESTINATION!!.coordinate).toString(),
                Toast.LENGTH_LONG).show()
            //Share/Publish Location

        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        prepareForegroundNotification()
        context = this
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
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
            Toast.makeText(context,"Bad", Toast.LENGTH_LONG).show()
            return
        }
        mFusedLocationClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback, Looper.myLooper()
        )
    }

    private fun prepareForegroundNotification() {
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
        //region data
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 3000
        private lateinit var context: Context
    }
}