package com.example.sparks

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class App : Application() {


    /*
    *
    * TODO("Napravi da bude samo jedan activity za podesavanja")
    *
    * */
    companion object{
        val channelID: String  = "channel1"
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

            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel1)

        }


    }
}