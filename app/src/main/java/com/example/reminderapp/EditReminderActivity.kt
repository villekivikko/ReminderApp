package com.example.reminderapp

import android.Manifest
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
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
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class EditReminderActivity : AppCompatActivity() , DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {
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
            finish()
        }

        //Initialize date and setOnClickListener
        binding.textDateEdit.inputType = InputType.TYPE_NULL
        binding.textDateEdit.isClickable=true
        binding.textDateEdit.setOnClickListener {
            reminderCalendar = GregorianCalendar.getInstance()
            DatePickerDialog(this, this,
                    reminderCalendar.get(Calendar.YEAR),
                    reminderCalendar.get(Calendar.MONTH),
                    reminderCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        //Initialize Delete button and setOnClickListener
        deleteBtn = binding.btnDelete
        deleteBtn.setOnClickListener {
            //Delete the reminder from database and go back to MainActivity
            val database = Firebase.database(getString(R.string.firebase_db_url))
            val reference = database.getReference(LoginActivity.usernameGlobal)
            if(key!=null) {
                reference.child(key).removeValue()
                //Delete the WorkManager work
                MainActivity.cancelReminder(applicationContext, key)
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
            else{
                time = binding.textDateEdit.text.toString()
            }
            if(latitude!=0.0){
                location_x = latitude
                location_y = longitude
            }

            val reminderCalendar = GregorianCalendar.getInstance()
            val dateFormat = "dd.MM.yyyy HH:mm"
            // If the reminder has a date and time
            if(binding.textDateEdit.text.toString() != "") {
                //This can be done with API version 26
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val formatter = DateTimeFormatter.ofPattern(dateFormat)
                    val date = LocalDateTime.parse(binding.textDateEdit.text, formatter)

                    reminderCalendar.set(Calendar.YEAR, date.year)
                    reminderCalendar.set(Calendar.MONTH, date.monthValue - 1)
                    reminderCalendar.set(Calendar.DAY_OF_MONTH, date.dayOfMonth)
                    reminderCalendar.set(Calendar.HOUR_OF_DAY, date.hour)
                    reminderCalendar.set(Calendar.MINUTE, date.minute)
                }
                //With lower apis we have to do it manually
                else {
                    if (dateFormat.contains(":")) {
                        //Split date and time to pieces
                        val dateParts = binding.textDateEdit.text.split(" ")
                                .toTypedArray()[0].split(".").toTypedArray()
                        val timeParts = binding.textDateEdit.text.split(" ")
                                .toTypedArray()[1].split(":").toTypedArray()

                        reminderCalendar.set(Calendar.YEAR, dateParts[2].toInt())
                        reminderCalendar.set(Calendar.MONTH, dateParts[1].toInt() - 1)
                        reminderCalendar.set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
                        reminderCalendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                        reminderCalendar.set(Calendar.MINUTE, timeParts[1].toInt())

                    } else {
                        //no time part
                        //convert date  string value to Date format using dd.mm.yyyy
                        // here it is assumed that date is in dd.mm.yyyy
                        val dateParts = binding.textDateEdit.text.split(".").toTypedArray()
                        reminderCalendar.set(Calendar.YEAR, dateParts[2].toInt())
                        reminderCalendar.set(Calendar.MONTH, dateParts[1].toInt() - 1)
                        reminderCalendar.set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
                    }
                }
            }

            if(binding.textDateEdit.text.toString() != ""){
                time = binding.textDateEdit.text.toString()
            }

            val reminder = ReminderInfo(key!!, message!!, location_x, location_y, time!!,
                    AddActivity.getCurrentTime(), LoginActivity.usernameGlobal, false)
            //Update firebase
            val database = Firebase.database(getString(R.string.firebase_db_url))
            val reference = database.getReference(LoginActivity.usernameGlobal)
            reference.child(key).setValue(reminder)

            //Set reminder with WorkManager. This replaces the old one.
            MainActivity.setReminderWithWorkManager(applicationContext, key,
                    reminderCalendar.timeInMillis, message!!, reminder.location_x,
            reminder.location_y)

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

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        reminderCalendar.set(Calendar.YEAR, year)
        reminderCalendar.set(Calendar.MONTH, month)
        reminderCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
        binding.textDateEdit.setText(simpleDateFormat.format(reminderCalendar.time))

        //Pick Time
        TimePickerDialog(
                this,
                this,
                reminderCalendar.get(Calendar.HOUR_OF_DAY),
                reminderCalendar.get(Calendar.MINUTE),
                true
        ).show()
    }

    override fun onTimeSet(view: TimePicker?, selectedhourOfDay: Int, selectedMinute: Int) {
        reminderCalendar.set(Calendar.HOUR_OF_DAY, selectedhourOfDay)
        reminderCalendar.set(Calendar.MINUTE, selectedMinute)
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
        binding.textDateEdit.setText(simpleDateFormat.format(reminderCalendar.time))
    }
}