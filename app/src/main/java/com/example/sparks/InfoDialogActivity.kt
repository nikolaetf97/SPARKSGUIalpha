package com.example.sparks

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class InfoDialogActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val lang = sharedPref.getString("LANG","sr")
        val locale = Locale(lang!!)

        Locale.setDefault(locale)

        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.information))


        val infoDialog = layoutInflater.inflate(R.layout.dialog_info, null)

        val pSpot = PSpotSupplier.parkingSpots
            .filter { spot -> spot.getMarker() == MainActivity.DESTINATION }[0]

        infoDialog.findViewById<TextView>(R.id.d_info_tv_arrival).text =
            getString(R.string.arrival_time) +
                    SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.US).format(Date(SPARKService.arrivalTime))
        infoDialog.findViewById<TextView>(R.id.d_info_tv_cost).text =
            getString(R.string.cost) + SPARKService.cost + "KM"
        infoDialog.findViewById<TextView>(R.id.d_info_tv_loc).text =
             getString(R.string.parking_name)  + pSpot.name
        infoDialog.findViewById<TextView>(R.id.d_info_tv_period).text =
            getString(R.string.info_tv_period) + SPARKService.cost * 5 + "min"
        infoDialog.findViewById<TextView>(R.id.d_info_tv_reg_num).text =
            getString(R.string.plates) + ": " + MainActivity.plates
        infoDialog.findViewById<TextView>(R.id.d_info_tv_zone).text =
            getString(R.string.zone_title) + ": " + pSpot.zone


        builder.setView(infoDialog)
        builder.setPositiveButton("Ok") { dialog, _ ->
            run {
                finish()
                dialog.dismiss()
            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun onRestart() {
        super.onRestart()
        val sharedPref = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val lang = sharedPref.getString("LANG","sr")
        val locale = Locale(lang!!)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}