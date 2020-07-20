package com.example.sparks

import android.os.AsyncTask
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.mapping.Map

/*
*
* Wrapper klasa za asinhrono izvrsavanje prikazivanja
* najblizih parkinga na mapi
*
* */

class ShowPreferableSpotsTask( val map: Map, val coordinate: GeoCoordinate): AsyncTask<Unit, Unit, Unit>() {
    override fun doInBackground(vararg params: Unit?): Unit {
        PSpotSupplier.showNearbyMarkers(3, coordinate)

    }
}