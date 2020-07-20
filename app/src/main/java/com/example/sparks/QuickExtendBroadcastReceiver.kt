package com.example.sparks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/*
*
* Na pritisak na dugme u notifikicaji
* se produzi parking ako moze
*
* */

class QuickExtendBroadcastReceiver() : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        ParkingCountdownTimer.setTime(3000000)  //quick extend for 5minutes
    }
}