package com.example.sparks

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class App : Application() {
    companion object{
        val channelID: String  = "channel1"
        var notificationManager: NotificationManager? = null
        val LOCATION_SERVICE = 222
        val LOCATION_SERVICE_REQUEST = 222222
    }

    override fun onCreate(){
        super.onCreate()
        createNotificationChannel()
        LogDataSupplier.init(applicationContext)
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel1 = NotificationChannel(
                channelID, "APP_NOTIFICATION_CHANNEL",
                NotificationManager.IMPORTANCE_HIGH
            )

            channel1.description = "ticket period countdown service"

            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel1)

        }


    }

}