package com.example.sparks

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.CollapsibleActionView
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    override fun onMapReady(p0: GoogleMap?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val MAPVIEW_BUNDLE_KEY: String? = "MapViewBundleKey"
    var isOpen = false


    private lateinit var mMapView: MapView
    private lateinit var mRandom: java.util.Random
    private lateinit var mHandler: Handler
    private lateinit var mRunnable:Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //fab.setBackgroundColor(Color.parseColor("#868686"))

        mMapView = findViewById(R.id.mapView)
        initGoogleMap(savedInstanceState);


        var fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        var fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        var rClockWise = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise)
        var rCounterClockWise= AnimationUtils.loadAnimation(this, R.anim.rotate_counterclockwise)



        fab.setOnClickListener { view ->
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

        fab_edit_1.setOnClickListener{ view ->
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


    }

    private fun initGoogleMap(savedInstanceState: Bundle?) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }

        mMapView.onCreate(mapViewBundle)

        mMapView.getMapAsync(this)
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
