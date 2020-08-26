package com.example.sparks

import android.os.AsyncTask
import com.here.android.mpa.common.GeoCoordinate
class ShowPreferableSpotsTask(private val coordinate: GeoCoordinate): AsyncTask<Unit, Unit, Unit>() {
    override fun doInBackground(vararg params: Unit?) {
        PSpotSupplier.showNearbyMarkers(3, coordinate)

    }
}