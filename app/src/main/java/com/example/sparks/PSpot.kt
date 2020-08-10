package com.example.sparks

import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.common.Image
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapMarker
import java.util.function.DoubleBinaryOperator

/*
*
* TODO("Optimizacije: tmp treba da bude jedna promjenjiva objekta, i da se mjenja kada se mjenja freeSpace")
*
* */

data class PSpot(val latitude:Double, val longitude:Double, var freeSpace: Int, val space: Int,
                 val name: String = "", val zone: Int = 0){

    private var marker: MapMarker = initMapMarker()

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

    /*
    *
    * U marker description polje upisujemo id ikone koju smo korisitili
    * da bi smo je mogli prikazati na infobubble kada se dodirne marker
    *
    * */


    private fun initMapMarker(): MapMarker{

        val toReturn = MapMarker(GeoCoordinate(latitude, longitude))
        val image = Image()

        chooseIcon(image, toReturn)
        /* val tmp = freeSpace/space

         if (tmp < 0.25){
             image.setImageResource(R.drawable.parking_pin_large_green)
             toReturn.description = R.drawable.parking_pin_large_green.toString()
         } else if (tmp < 0.5){
             image.setImageResource(R.drawable.parking_pin_large_yellow)
             toReturn.description = R.drawable.parking_pin_large_yellow.toString()
         } else {
             image.setImageResource(R.drawable.parking_pin_large_red)
             toReturn.description = R.drawable.parking_pin_large_red.toString()
         }


        toReturn.icon = image
        toReturn.isVisible = false
*/

        return toReturn
    }

    private fun setMarkerIcon(){

        val image = Image()
        chooseIcon(image, marker)

        /* val tmp = freeSpace/space

         if (tmp < 0.25){
             image.setImageResource(R.drawable.parking_pin_large_green)
             marker.description = R.drawable.parking_pin_large_green.toString()
         } else if (tmp < 0.5){
             image.setImageResource(R.drawable.parking_pin_large_yellow)
             marker.description = R.drawable.parking_pin_large_yellow.toString()
         } else {
             image.setImageResource(R.drawable.parking_pin_large_red)
             marker.description = R.drawable.parking_pin_large_red.toString()
         }*/
    }

    private fun chooseIcon(image: Image, marker: MapMarker){
        val tmp = freeSpace.toDouble() / space.toDouble()

        if (tmp < 0.25){
            image.setImageResource(R.drawable.parking_pin_large_green)
            marker.description = R.drawable.parking_pin_large_green.toString()
        } else if (tmp < 0.5){
            image.setImageResource(R.drawable.parking_pin_large_yellow)
            marker.description = R.drawable.parking_pin_large_yellow.toString()
        } else {
            image.setImageResource(R.drawable.parking_pin_large_red)
            marker.description = R.drawable.parking_pin_large_red.toString()
        }

        marker.icon = image
        marker.isVisible = false
    }

    fun getMarker(): MapMarker = marker

    fun shrinkMarker() {
        val tmp = freeSpace.toDouble() / space.toDouble()
        val image = Image()

        if (tmp < 0.25){
            image.setImageResource(R.drawable.parking_pin_large_green)
        } else if (tmp < 0.5){
            image.setImageResource(R.drawable.parking_pin_large_yellow)
        } else {
            image.setImageResource(R.drawable.parking_pin_large_red)
        }

        marker.icon = image
    }

    fun expandMarker() {
        val tmp = freeSpace.toDouble() / space.toDouble()
        val image = Image()

        if (tmp < 0.25){
            image.setImageResource(R.drawable.parking_pin_larger_green)
        } else if (tmp < 0.5){
            image.setImageResource(R.drawable.parking_pin_larger_yellow)
        } else {
            image.setImageResource(R.drawable.parking_pin_larger_red)
        }

        marker.icon = image
    }

}
/*
* Dodao sam da posalje numberOfSpaces, da bi mogao racunati zauzetost
* pretpostavka da je parking jedinstveno identifikovan
* svojom geografskom pozicijom i imenom
* */

object PSpotSupplier{

    private val maps = mutableListOf<Map>()

    fun addMap(map: Map) = maps.add(map)

    lateinit var  parkingSports: MutableSet<PSpot>

    fun addPSpot(spot: PSpot){
        parkingSports.add(spot)
        for(map in maps)
            map.addMapObject(spot.getMarker())
    }

    fun showNearbyMarkers(
        numOfSpots: Int,
        coordinate: GeoCoordinate
    ): List<PSpot> {
        val spots = parkingSports.sortedBy {
                parkingSpot ->  coordinate.distanceTo(parkingSpot.getMarker().coordinate)
        }.subList(0, numOfSpots)

        for( spot in parkingSports){
            spot.getMarker().isVisible = false
        }
        for (spot in spots){
            spot.getMarker().isVisible = true
        }


        return spots
    }

    fun init(){
        parkingSports = mutableSetOf(PSpot(44.809049, 17.209781, 20, 50, "Bingo"),
            PSpot(44.838102, 17.220876, 12, 120, "Centrum"),
            PSpot(44.817937, 17.216730, 100, 120, "Hiper Kort"),
            PSpot(44.816687, 17.211028, 50, 300, "FIS"),
            PSpot(44.799300, 17.207989, 12, 100, "Zoki Komerc"))
    }
}

/*
* Dobra stvar sa object tipom u Kotlinu je sto je to u principu thread-safe singleton, tako da kada
* servis za update stanja parkinga bude menjao stanje nece remetiti
* rad ostatka apllkacije
* */
