package com.example.sparks

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.MapView
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.mapviewlite.MapStyle
import com.here.sdk.mapviewlite.MapViewLite
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    var isOpen = false

    private var mapView: MapViewLite? = null

    private lateinit var mMapView: MapView
    private lateinit var mRandom: java.util.Random
    private lateinit var mHandler: Handler
    private lateinit var mRunnable:Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        var fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        var fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        var rClockWise = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise)
        var rCounterClockWise= AnimationUtils.loadAnimation(this, R.anim.rotate_counterclockwise)



        fab.setOnClickListener {
            if (isOpen){
                fab_edit_1.startAnimation(fabClose)
                fab_edit_2.startAnimation(fabClose)

                fab.startAnimation(rClockWise)
                isOpen = false

            } else{
                fab_edit_1.startAnimation(fabOpen)
                fab_edit_2.startAnimation(fabOpen)

                fab.startAnimation(rCounterClockWise)
                isOpen = true

                fab_edit_1.isClickable
                fab_edit_2.isClickable
            }
        }

        fab_edit_1.setOnClickListener{
            startActivity(Intent(this, SelectParkingForm::class.java))
        }

        mHandler = Handler()
        mRandom = Random()

        swipeContainer.setOnRefreshListener {

            mRunnable = Runnable {
                swipeContainer.isRefreshing = false
            }

            mHandler.postDelayed(mRunnable, ((Random().nextInt(4) + 1)*1000).toLong())
        }
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_red_dark)

        var mapViewVar: MapViewLite? = findViewById(R.id.map_view)


        mapViewVar?.onCreate(savedInstanceState)
        mapViewVar?.getMapScene()?.loadScene(
            MapStyle.NORMAL_DAY
        ) { errorCode ->
            if (errorCode == null) {
                mapViewVar.getCamera().target = GeoCoordinates(52.530932, 13.384915)
                mapViewVar.getCamera().zoomLevel = 14.0
            } else {
                Log.d(FragmentActivity.UI_MODE_SERVICE, "onLoadScene failed: $errorCode")
            }
        }

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
