package com.example.sparks

import android.app.IntentService
import android.content.Intent

class ShowInfoIntentService: IntentService("ShowInfoIntentService") {
    override fun onHandleIntent(p0: Intent?) {
        val intent = Intent(this, InfoDialogActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME)
        startActivity(intent)
    }
}