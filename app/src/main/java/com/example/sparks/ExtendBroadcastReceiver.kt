package com.example.sparks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
class ExtendBroadcastReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        ParkingCountdownTimer.setTime(DEFAULT_EXTEND)  //quick extend for 1 minute
        SPARKService.cost++
    }
    companion object{
        const val DEFAULT_EXTEND: Long = 60000
    }
}