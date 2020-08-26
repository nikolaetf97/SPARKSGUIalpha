package com.example.sparks

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat.startForegroundService
import androidx.work.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit

class CheckArrivalForegroundService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        WorkManager
            .getInstance(applicationContext)
            .enqueue(PeriodicWorkRequest
                .Builder(CheckArrivalWorker::class.java, 5, TimeUnit.SECONDS)
                .build())
        return START_NOT_STICKY
    }

    override fun onCreate() {
    }

    override fun onDestroy() {
    }


}

class CheckArrivalWorker(
    context: Context, workerParams: WorkerParameters
): Worker(context, workerParams){

    override fun doWork(): Result {

        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, "Current pos: " + MainActivity.posManager!!.position.coordinate, Toast.LENGTH_LONG).show()
        }


        Thread.sleep(4000)

        if(MainActivity
                .DESTINATION!!
                .coordinate
                .distanceTo(MainActivity.posManager!!.position.coordinate) < 30){

            val serviceIntent = Intent(applicationContext, CheckLeavingService::class.java)

            startForegroundService(applicationContext, serviceIntent)

            WorkManager.getInstance(applicationContext).cancelUniqueWork(TAG)
        }else{
            val nextInstance = OneTimeWorkRequestBuilder<CheckArrivalWorker>().build()

            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, nextInstance)
        }

        return Result.success()
    }



    companion object {
        val TAG: String = "CHECK_ARRIVAL"
    }
}