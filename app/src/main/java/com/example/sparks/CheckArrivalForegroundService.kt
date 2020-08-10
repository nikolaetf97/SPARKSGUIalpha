package com.example.sparks

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.core.content.ContextCompat.startForegroundService
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
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

        Toast.makeText(applicationContext, "Checking arrival...", Toast.LENGTH_LONG).show()

        if(MainActivity
                .DESTINATION!!
                .coordinate
                .distanceTo(SelectParkingActivity.posManager!!.position.coordinate) < 30){

            val serviceIntent = Intent(applicationContext, CheckLeavingService::class.java)

                startForegroundService(applicationContext, serviceIntent)

            WorkManager.getInstance(applicationContext).cancelUniqueWork(TAG)
        }

        return Result.success()
    }



    companion object {
        val TAG: String = "CHECK_ARRIVAL"
    }
}