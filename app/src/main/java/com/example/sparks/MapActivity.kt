package com.example.sparks

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.mapviewlite.*

class MapActivity: AppCompatActivity() {

    private var mapView: MapViewLite? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var mapViewVar: MapViewLite? = findViewById(R.id.map_view_main)


        mapViewVar?.onCreate(savedInstanceState)
        mapViewVar?.getMapScene()?.loadScene(
            MapStyle.NORMAL_DAY
        ) { errorCode ->
            if (errorCode == null) {
                mapViewVar.getCamera().target = GeoCoordinates(44.7722, 17.1910)
                mapViewVar.getCamera().zoomLevel = 16.0
            } else {
                Log.d(FragmentActivity.UI_MODE_SERVICE, "onLoadScene failed: $errorCode")
            }
        }
        var mapImage = MapImageFactory.fromResource(resources, R.drawable.parking_pin)

        var mapMarker: MapMarker = MapMarker(GeoCoordinates(44.7690, 17.1885))
        var mapMarker2: MapMarker = MapMarker(GeoCoordinates(44.7682, 17.1855))
        var mapMarker3: MapMarker = MapMarker(GeoCoordinates(44.7676, 17.1893))

        mapMarker.addImage(mapImage, MapMarkerImageStyle())
        mapMarker2.addImage(mapImage, MapMarkerImageStyle())
        mapMarker3.addImage(mapImage, MapMarkerImageStyle())


        mapView?.mapScene?.addMapMarker(mapMarker)
        mapView?.mapScene?.addMapMarker(mapMarker3)
        mapView?.mapScene?.addMapMarker(mapMarker2)

        mapView = mapViewVar

    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

}