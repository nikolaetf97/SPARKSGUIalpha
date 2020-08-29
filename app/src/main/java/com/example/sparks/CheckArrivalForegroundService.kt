package com.example.sparks

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat.startForegroundService
import androidx.work.*
import com.here.android.mpa.common.PositioningManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit

class CheckArrivalForegroundService: Service() {
    private var handler: Handler? = null
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        Toast.makeText(this, "Check position service created!", Toast.LENGTH_LONG).show()
        handler = Handler()
        runnable = Runnable {

            //Toast.makeText(context, "Servicse is still running", Toast.LENGTH_LONG).show()
            Toast.makeText(this, posManager.position.coordinate.toString(), Toast.LENGTH_LONG).show()
            handler!!.postDelayed(runnable!!, 4000)
        }
        handler!!.postDelayed(runnable!!, 5000)
    }

    override fun onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        handler!!.removeCallbacks(runnable!!)
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show()
    }

    override fun onStart(intent: Intent, startid: Int) {
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show()
        if (!hasStarted){
            posManager =  PositioningManager.getInstance()
            posManager.start(PositioningManager.LocationMethod.GPS_NETWORK)
            hasStarted = true
        }

    }

    companion object {
        var runnable: Runnable? = null
        var hasStarted = false
        private lateinit var posManager: PositioningManager
    }


}

class CheckArrivalWorker(
    context: Context, workerParams: WorkerParameters
): Worker(context, workerParams){

    override fun doWork(): Result {

        val posManager = PositioningManager.getInstance()
            posManager.start(PositioningManager.LocationMethod.GPS_NETWORK)
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, Resources.getSystem().getString(R.string.position) + MainActivity.posManager!!.position.coordinate, Toast.LENGTH_LONG).show()
            Toast.makeText(applicationContext, "Current pos: " + posManager.position.coordinate, Toast.LENGTH_LONG).show()
            Toast.makeText(applicationContext, "Distance: " + MainActivity
                .DESTINATION!!
                .coordinate
                .distanceTo(posManager.position.coordinate).toString(), Toast.LENGTH_LONG).show()
        }


        Thread.sleep(4000)

        if(MainActivity
                .DESTINATION!!
                .coordinate
                .distanceTo(posManager.position.coordinate) < 30){

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