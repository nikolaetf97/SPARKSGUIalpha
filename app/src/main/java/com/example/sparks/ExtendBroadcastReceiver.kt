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

class ExtendBroadcastReceiver() : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        ParkingCountdownTimer.setTime(DEFAULT_EXTEND)  //quick extend for 5minutes
    }

    companion object{
        val DEFAULT_EXTEND = 3000000L
    }
}