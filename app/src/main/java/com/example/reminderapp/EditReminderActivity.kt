package com.example.reminderapp

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.reminderapp.databinding.ActivityEditReminderBinding
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class EditReminderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditReminderBinding
    private lateinit var applyBtn: Button
    private lateinit var deleteBtn: Button
    private lateinit var locationBtn: Button
    private lateinit var reminderCalendar: Calendar
    private lateinit var geofencingClient: GeofencingClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditReminderBinding.inflate(layoutInflater)
        val view = binding.root
        geofencingClient = LocationServices.getGeofencingClient(this)
        setContentView(view)


        //Set navigation bar item click listener
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.ic_home -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                }
                R.id.ic_add -> {
                    startActivity(Intent(applicationContext, AddActivity::class.java))
                    finish()
                }
                R.id.ic_profile -> {
                    startActivity(Intent(applicationContext, ProfileActivity::class.java))
                    finish()
                }
            }
            true
        }

        val key = intent.getStringExtra("key")
        var message = intent.getStringExtra("message")
        var location_x = intent.getDoubleExtra("latitude", 0.0)
        var location_y = intent.getDoubleExtra("longitude", 0.0)
        val latitude = intent.getDoubleExtra("latitudeNew", 0.0)
        val longitude = intent.getDoubleExtra("longitudeNew", 0.0)
        var time = intent.getStringExtra("time")
        if(latitude!=0.0){
            location_x = latitude
            location_y = longitude
        }

        runOnUiThread {
            binding.textNameEdit.hint = message
            binding.textDateEdit.hint = time
        }

        //Initialize Location button and setOnClickListener
        locationBtn = binding.btnLocation
        locationBtn.setOnClickListener {
            val intent = Intent(this, EditLocationActivity::class.java)
            intent.putExtra("key", key)
                    .putExtra("message", message)
                    .putExtra("latitude", location_x)
                    .putExtra("longitude", location_y)
                    .putExtra("time", time)
            startActivity(intent)

        }

        //Initialize Delete button and setOnClickListener
        deleteBtn = binding.btnDelete
        deleteBtn.setOnClickListener {
            //Delete the reminder from database and go back to MainActivity
            val database = Firebase.database(getString(R.string.firebase_db_url))
            val reference = database.getReference("User")
            if(key!=null) {
                reference.child(key).removeValue()
            }
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }

        //Initialize Apply button and setOnClickListener
        applyBtn = binding.btnApply
        applyBtn.setOnClickListener {
            //Check changes, if found -> update
            if (binding.textNameEdit.text.toString() != "") {
                message = binding.textNameEdit.text.toString()
            }
            if (binding.textDateEdit.text.toString() != "") {
                time = binding.textDateEdit.hint.toString()
            }
            // TODO: IMPLEMENT DATEPICKER AND CHECK CHANGES
            if(latitude!=0.0){
                location_x = latitude
                location_y = longitude
            }
            val reminder = ReminderInfo(key!!, message!!, location_x, location_y, time!!,
                    AddActivity.getCurrentTime(), "User", false)
            //Update firebase
            val database = Firebase.database(getString(R.string.firebase_db_url))
            val reference = database.getReference("User")
            reference.child(key).setValue(reminder)

            //TODO:  Delete old WorkManager
            //Set reminder with WorkManager
            val reminderCalendar = GregorianCalendar.getInstance()
            if(binding.textDateEdit.text.toString() != "") {
                MainActivity.setReminderWithWorkManager(applicationContext, key,
                        reminderCalendar.timeInMillis, binding.textNameEdit.text.toString())
            }

            //TODO:  Delete old geofence
            //Set Geofence reminder
            if(latitude != 0.0) {
                createGeoFence(LatLng(latitude, longitude), key,
                        binding.textNameEdit.text.toString(),  geofencingClient)
            }

            //Return to MainActivity
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
    }

    private fun createGeoFence(location: LatLng, key: String, msg: String, geofencingClient: GeofencingClient) {
        val geofence = Geofence.Builder()
                .setRequestId(GEOFENCE_ID)
                .setCircularRegion(location.latitude, location.longitude, GEOFENCE_RADIUS.toFloat())
                .setExpirationDuration(GEOFENCE_EXPIRATION.toLong())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(GEOFENCE_DWELL_DELAY)
                .build()

        val geofenceRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()


        val intent = Intent(this, GeofenceReceiver::class.java)
                .putExtra("key", key)
                .putExtra("message", "Geofence entered - $msg")

        val pendingIntent = PendingIntent.getBroadcast(
                applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                            applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        GEOFENCE_LOCATION_REQUEST_CODE
                )
            } else {
                geofencingClient.addGeofences(geofenceRequest, pendingIntent)
            }
        } else {
            geofencingClient.addGeofences(geofenceRequest, pendingIntent)
        }
    }
}