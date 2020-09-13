package com.example.sparks

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.common.Image
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapMarker
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.stringify
import java.io.File
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

@Serializable
data class PSpot(val latitude:Double, val longitude:Double, var freeSpace: Int, val space: Int,
                 val name: String = "", val zone: Int = 0){

    @Transient
    private var marker: MapMarker = initMapMarker()


    @Transient
    private var hasChanged = false


    override fun equals(other: Any?): Boolean {

        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as PSpot

        return other.latitude == latitude && other.longitude == longitude
    }

    override fun hashCode(): Int = name.hashCode()


    fun setFreeSpaces(set: Int){
        freeSpace = set
        hasChanged = true
        setMarkerIcon()
    }

    private fun initMapMarker(): MapMarker{

        val toReturn = MapMarker(GeoCoordinate(latitude, longitude))
        val image = Image()

        chooseIcon(image, toReturn)

        return toReturn
    }

    private fun setMarkerIcon(){

        val image = Image()
        chooseIcon(image, marker)
    }

    private fun chooseIcon(image: Image, marker: MapMarker){
        val tmp = freeSpace.toDouble() / space.toDouble()

        if (tmp < 0.25){
            image.setImageResource(R.drawable.parking_pin_large_red)
            marker.description = R.drawable.parking_pin_large_red.toString()
        } else if (tmp < 0.5){
            image.setImageResource(R.drawable.parking_pin_large_yellow)
            marker.description = R.drawable.parking_pin_large_yellow.toString()
        } else {
            image.setImageResource(R.drawable.parking_pin_large_green)
            marker.description = R.drawable.parking_pin_large_green.toString()
        }

        marker.icon = image
        if(!hasChanged)
            marker.isVisible = false
    }

    fun getMarker(): MapMarker = marker

    fun shrinkMarker() {
        val tmp = freeSpace.toDouble() / space.toDouble()
        val image = Image()

        if (tmp < 0.25){
            image.setImageResource(R.drawable.parking_pin_large_red)
        } else if (tmp < 0.5){
            image.setImageResource(R.drawable.parking_pin_large_yellow)
        } else {
            image.setImageResource(R.drawable.parking_pin_large_green)
        }

        marker.icon = image
    }

    fun expandMarker() {
        val tmp = freeSpace.toDouble() / space.toDouble()
        val image = Image()

        if (tmp < 0.25){
            image.setImageResource(R.drawable.parking_pin_larger_red)
        } else if (tmp < 0.5){
            image.setImageResource(R.drawable.parking_pin_larger_yellow)
        } else {
            image.setImageResource(R.drawable.parking_pin_larger_green)
        }

        marker.icon = image
    }

    override fun toString(): String {
        return name + " " + latitude.toString() + " " + longitude.toString() + " " + freeSpace.toString() + " " + space.toString()
    }

}

object PSpotSupplier{

    private var map: Map? = null

    fun addMap(toAdd: Map){
        this.map = toAdd
    }

    var  parkingSpots: MutableSet<PSpot> = mutableSetOf()


    fun getNames(): List<String>{
        return parkingSpots.map { it.name }
    }

    fun addPSpot(spot: PSpot){
        parkingSpots.add(spot)
        map!!.addMapObject(spot.getMarker())
    }

    fun showNearbyMarkers(
        numOfSpots: Int,
        coordinate: GeoCoordinate
    ): List<PSpot> {
        for( spot in parkingSpots){
            Log.d("spots", spot.toString())
            spot.getMarker().isVisible = false
        }
        val spots = parkingSpots.sortedBy {
                parkingSpot ->  coordinate.distanceTo(parkingSpot.getMarker().coordinate)
        }.subList(0, numOfSpots)

        for( spot in parkingSpots){
            spot.getMarker().isVisible = false
        }
        for (spot in spots){
            spot.getMarker().isVisible = true
        }

        spots[0].expandMarker()
        MainActivity.DESTINATION = spots[0].getMarker()

        return spots
    }

    fun init(applicationContext: Context) {
        val psFile = File(applicationContext.getExternalFilesDir(null)!!.path, "ps.pspots")

        if (!psFile.exists())
            loadTestPSpots(applicationContext, psFile)
        else {

            val tmp = Json {}
                .decodeFromJsonElement<Set<PSpot>>(
                    Json {}
                        .parseToJsonElement(psFile.readText())
                )

            parkingSpots.addAll(tmp)

            for(ps in parkingSpots){
                Log.d("init", ps.toString())
            }
        }
    }

    fun loadTestPSpots(applicationContext: Context, psFile: File) {
        parkingSpots = mutableSetOf(PSpot(44.809049, 17.209781, 20, 50, "Bingo"),
            PSpot(44.838102, 17.220876, 12, 120, "Centrum"),
            PSpot(44.817937, 17.216730, 100, 120, "Hiper Kort"),
            PSpot(44.816687, 17.211028, 50, 300, "FIS"),
            PSpot(44.799300, 17.207989, 12, 100, "Zoki Komerc"),
            PSpot(44.819054, 17.210573, 8, 12, "Test"),
            PSpot(44.766756, 17.1872528, 3, 12, "Test2"))

        val tmp = Json{}
            .encodeToString(SetSerializer(PSpot.serializer()),
            parkingSpots)

        psFile.writeText(tmp)

    }

    fun setFreeSpaces(t:PSpot){
        val ps = parkingSpots.find { spot -> spot == t }

        Log.d(ps!!.name, "asdasdas")

        ps!!.setFreeSpaces(t.freeSpace)
        map!!.removeMapObject(ps!!.getMarker())
        map!!.addMapObject(ps!!.getMarker())
    }
}
