package com.example.sparks

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.widget.Toast
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import java.io.File

class App : Application() {
    companion object{
        val channelID: String  = "channel1"
        var notificationManager: NotificationManager? = null
        val LOCATION_SERVICE = 111
        val LOCATION_SERVICE_REQUEST = 111111
    }

    override fun onCreate(){
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel1 = NotificationChannel(
                channelID, "TICKET_SERVICE",
                NotificationManager.IMPORTANCE_HIGH
            )

            channel1.description = "ticket period countdown service"

            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel1)

        }


    }

}