package com.example.sparks

import android.app.Service
import android.content.Intent
import android.os.IBinder

class CheckLeavingService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Napisi ovaj servis da prikazuje onaj tajmer u notifikaciji i " +
                "da se pokrene iz onArrival workera")
    }
}