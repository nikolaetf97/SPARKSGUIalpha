package com.example.sparks

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import androidx.work.*
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import org.json.JSONArray
import org.json.JSONObject

class GetPSpotsBackgroundService : Service() {
    private var handler: Handler? = null
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        Toast.makeText(this, getString(R.string.service_create), Toast.LENGTH_LONG).show()
        val parkingService = GetPSpotsTask
        handler = Handler()
        runnable = Runnable {
            parkingService.execute()
            handler!!.postDelayed(runnable!!, 10000)
        }
        handler!!.postDelayed(runnable!!, 15000)
    }

    override fun onDestroy() {
        handler!!.removeCallbacks(runnable!!)
        Toast.makeText(this, getString(R.string.service_stop), Toast.LENGTH_LONG).show()
    }

    override fun onStart(intent: Intent, startid: Int) {
        Toast.makeText(this, getString(R.string.service_start), Toast.LENGTH_LONG).show()
    }

    companion object {
        var runnable: Runnable? = null
    }
}

class GetPSpotsWorker(context: Context,
                workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    override fun doWork(): Result {
        Thread.sleep(5000)

        val result = getJSON()

        val nextWorker = OneTimeWorkRequestBuilder<GetPSpotsWorker>().build()
        WorkManager
            .getInstance(applicationContext)
            .enqueueUniqueWork(GET_PSPOT_TAG, ExistingWorkPolicy.REPLACE, nextWorker)

        return result
    }

    private fun getJSON(): Result{
        return try{
            val res = OkHttpClient().newCall(Request.Builder()
                .url("http://192.168.0.104:8080/RestParking/api/service").build())
                .execute().body().toString()

            Result
                .success(Data.Builder().putString("JSON", res)
                    .build())
        } catch (e: Exception){
            e.printStackTrace()

            Result.failure(Data.Builder()
                .putString("JSON", "ERROR").build())
        }
    }

    companion object{
        const val GET_PSPOT_TAG = "getPSpotsTag"
    }
}

class ProcessPSpotsWorker(context: Context,
                    workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    override fun doWork(): Result {
        val processPSpots = processPSpots()

        val nextWorker = OneTimeWorkRequestBuilder<ProcessPSpotsWorker>().build()
        WorkManager
            .getInstance(applicationContext)
            .enqueueUniqueWork(PROCESS_PSPOT_TAG, ExistingWorkPolicy.REPLACE, nextWorker)

        return  processPSpots
    }

    private fun processPSpots(): Result {
        return try {
            val result = inputData.getString("JSON")

            if(result != "ERROR")
                throw Exception("Error in worker data")
            else{
                val jsonArray= JSONArray(result)
                for(i in 0 until jsonArray.length()) {
                    val destination: JSONObject =
                        jsonArray.getJSONObject(i).getJSONObject("destination")

                    val tmp = PSpot(
                        destination.get("latitude").toString().toDouble(),
                        destination.get("longitude").toString().toDouble(),
                        destination.get("free_spaces").toString().toInt(),
                        destination.get("spaces").toString().toInt(),
                        destination.get("name").toString())

                    if (PSpotSupplier.parkingSpots.contains(tmp)) {
                        PSpotSupplier.setFreeSpaces(tmp)
                    } else
                        PSpotSupplier.addPSpot(tmp)
                }
            }
            Result
                .success()
        } catch (e: Exception){
            Result
                .failure(Data
                    .Builder()
                    .putString("RES", e.localizedMessage)
                    .build())
        }
    }

    companion object{
        const val PROCESS_PSPOT_TAG = "processPSpotsTag"
    }
}

object GetPSpotsTask: AsyncTask<Unit, Unit, String>(){

    override fun doInBackground(vararg params: Unit?): String? {
        val client = OkHttpClient()

        val request =
            Request
                .Builder()
                .url("http://192.168.0.104:8080/RestParking/api/service")
                .build()

        val response: Response?
        try {
            response = client
                .newCall(request)
                .execute()

            println(response.body())

            return response.body().string()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null

    }

    override fun onPostExecute(result: String?) {

        /*if(result!=null){
            //ovde treba to procitati JSON

            val jsonArray= JSONArray(result)
            for(i in 0 until jsonArray.length()){
                val destination: JSONObject =jsonArray.getJSONObject(i).getJSONObject("destination")

                val tmp = PSpot(destination.get("latitude").toString().toDouble(),
                    destination.get("longitude").toString().toDouble(),
                    destination.get("free_spaces").toString().toInt(),
                    destination.get("spaces").toString().toInt(),
                    destination.get("name").toString())

                if(PSpotSupplier.parkingSports.contains(tmp)){
                    PSpotSupplier.parkingSports.find { spot -> spot == tmp }!!.setFreeSpaces(tmp.freeSpace)
                } else
                    PSpotSupplier.addPSpot(tmp)

                /*val image= Image()
                //getPin(image, freeSpaces, spaces)
                //po je PointF
                var point= PointF()
                point.x=longitude.toFloat()
                point.y=latitude.toFloat()
                println(i.toString()+"sada je na redu"+latitude+" "+longitude+"stvarna lokacija"+point.x.toString()+" "+point.y.toString())
                if(map2==null){
                    println("Null je mapa")
                }
                if(point==null){
                    println("Null je point")
                }
                var geo= GeoCoordinate(longitude.toDouble(),latitude.toDouble())
                var parkingSpot = MapMarker(geo, image)
                parkingSpot.title="Hello"
                //var parkingSpot = MapMarker(map2!!.pixelToGeo(point)!!, image)


                map2?.addMapObject(parkingSpot!!)*/

            }
            //var jsonOObj:List<JSONObject>=List<JSONObject>(result);
            //text2.setText(jsonOObj.length());
        }*/
    }
}