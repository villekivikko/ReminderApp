package com.example.reminderapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import android.os.Build
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.text.InputType
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.reminderapp.databinding.ActivityAddBinding
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

const val REQUEST_CODE_SPEECH = 100

class AddActivity : AppCompatActivity() , DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: ActivityAddBinding
    private lateinit var locationBtn: Button
    private lateinit var addBtn: Button
    private lateinit var voiceBtn: ImageButton
    private lateinit var reminderCalendar: Calendar
    private lateinit var geofencingClient: GeofencingClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        val view = binding.root
        geofencingClient = LocationServices.getGeofencingClient(this)
        setContentView(view)

        //Set navigation bar item click listener
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.ic_home ->  {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                }
                R.id.ic_profile ->  {
                    startActivity(Intent(applicationContext, ProfileActivity::class.java))
                    finish()
                }
            }
            true
        }

        //Initialize location button and setOnClickListener
        locationBtn = binding.btnLocation
        locationBtn.setOnClickListener {
            startActivity(Intent(applicationContext, MapActivity::class.java))
            finish()
        }

        //Initialize voice button and setOnClickListener
        voiceBtn = binding.btnVoice
        voiceBtn.setOnClickListener {
            val voiceIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault())
            voiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something...")
            try{
                startActivityForResult(voiceIntent, REQUEST_CODE_SPEECH)
            }catch(e: Exception){
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
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

        //Get latitude and longitude
        val latitude = intent.getDoubleExtra("Latitude", 0.0)
        val longitude = intent.getDoubleExtra("Longitude", 0.0)

        //Initialize add button and setOnClickListener
        addBtn = binding.btnAdd
        addBtn.setOnClickListener {

            // If the reminder has a date and time
            if(binding.textDateEdit.text.toString() != "") {
                reminderCalendar = getTimeInCalendar(binding.textDateEdit.text.toString())
            }

            //Add reminder to Firebase database
            if (binding.textNameEdit.text.toString() != "") {

                val database = Firebase.database(getString(R.string.firebase_db_url))
                val reference = database.getReference(LoginActivity.usernameGlobal)
                val key = reference.push().key

                if (key != null) {
                    val reminder = ReminderInfo(key, binding.textNameEdit.text.toString(), latitude, longitude,
                        binding.textDateEdit.text.toString(),
                        getCurrentTime(), LoginActivity.usernameGlobal, false)

                    //If the reminder has no time or location set
                    if(reminder.reminder_time == "" && reminder.location_x == 0.0){
                        reminder.reminder_seen = true
                    }
                    reference.child(key).setValue(reminder)

                    //Set reminder with WorkManager
                    if(binding.textDateEdit.text.toString() != "") {
                        MainActivity.setReminderWithWorkManager(applicationContext, key,
                                reminderCalendar.timeInMillis, binding.textNameEdit.text.toString(),
                        reminder.location_x, reminder.location_y)
                    }

                    //Set Geofence reminder
                    if(latitude != 0.0) {
                        createGeoFence(applicationContext, LatLng(latitude, longitude), key,
                                binding.textNameEdit.text.toString(),  geofencingClient)
                    }
                }
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }
            else {
                Toast.makeText(
                        applicationContext,
                        "Reminder must contain a message!",
                        Toast.LENGTH_SHORT
                ).show()
                getCurrentTime()
                return@setOnClickListener
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CODE_SPEECH ->{
                if(resultCode== Activity.RESULT_OK && data != null){
                    // get the text
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    binding.textNameEdit.setText(result!![0])
                }
            }
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

    companion object{

        fun createGeoFence(context: Context, location: LatLng, key: String, msg: String, geofencingClient: GeofencingClient) {
            val geofence = Geofence.Builder()
                    .setRequestId(key)
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

            val intent = Intent(context, GeofenceReceiver::class.java)
                    .putExtra("key", key)
                    .putExtra("message", "Geofence entered - $msg")

            val pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            Activity(),
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

        fun getCurrentTime(): String{
            val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
            return simpleDateFormat.format(Date())
        }

        fun getTimeInCalendar(time: String): Calendar{
            val reminderCalendar = GregorianCalendar.getInstance()
            val dateFormat = "dd.MM.yyyy HH:mm"
            //This can be done with API version 26
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val formatter = DateTimeFormatter.ofPattern(dateFormat)
                val date = LocalDateTime.parse(time, formatter)

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
                    val dateParts = time.split(" ")
                            .toTypedArray()[0].split(".").toTypedArray()
                    val timeParts = time.split(" ")
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
                    val dateParts = time.split(".").toTypedArray()
                    reminderCalendar.set(Calendar.YEAR, dateParts[2].toInt())
                    reminderCalendar.set(Calendar.MONTH, dateParts[1].toInt() - 1)
                    reminderCalendar.set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
                }
            }
            return reminderCalendar
        }
    }
}