package com.example.reminderapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import com.example.reminderapp.databinding.ActivityMapBinding
import com.example.reminderapp.db.ReminderInfo

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapBinding
    private lateinit var alertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapId) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val currentCity = LatLng(65.01355,25.46401)
        val zoomLevel = 15f

        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(currentCity, zoomLevel)
        )

        onLongClick(map)
        setPoiOnClick(map)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun onLongClick(googleMap: GoogleMap){
        googleMap.setOnMapLongClickListener {

            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Lng: %2$.5f",
                it.latitude,
                it.longitude
            )
            val lat = it.latitude
            val long = it.longitude

            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(it)
                .title("Reminder location")
            )
            showPopUp(lat, long)
        }
    }

    fun showPopUp(lat: Double, long: Double) {
        val inflater: LayoutInflater = this.getLayoutInflater()
        val dialogView: View = inflater.inflate(R.layout.popup, null)

        val buttonNo: Button = dialogView.findViewById(R.id.btnNo)
        buttonNo.setOnClickListener {
            alertDialog.cancel()
        }

        val buttonYes: Button = dialogView.findViewById(R.id.btnYes)
        buttonYes.setOnClickListener {
            //Transfer latitude and longitude to database
            val database = Firebase.database(getString(R.string.firebase_db_url))
            val reference = database.getReference("Location")
            reference.push().setValue(lat, long)

            alertDialog.cancel()
            finish()

        }

        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogView)

        alertDialog = dialogBuilder.create();
        alertDialog.show()
        alertDialog.getWindow()?.setLayout(1200, 700); //Controlling width and height.
    }
    }

    private fun setPoiOnClick(googleMap: GoogleMap){
        googleMap.setOnPoiClickListener { poi ->
            val poiMarker = googleMap.addMarker(
                MarkerOptions().position(poi.latLng)
                    .title(poi.name)
            )
        }
    }

    private fun setLocation(latitude: Double, longitude: Double){
        val lat = latitude
        val long = longitude
    }

    fun getLocation(latitude: Double, longitude: Double): Array<Double> {
        return arrayOf(latitude, longitude)
    }
/*
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
 */
