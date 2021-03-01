package com.example.reminderapp

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
import android.app.TimePickerDialog
import android.text.InputType
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.reminderapp.databinding.ActivityAddBinding
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Set navigation bar item click listener
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.ic_home ->  {startActivity(
                    Intent(applicationContext, MainActivity::class.java)
                )
                    finish()
                }
                R.id.ic_profile ->  {startActivity(
                    Intent(applicationContext, ProfileActivity::class.java)
                )
                    finish()
                }
            }
            true
        }

        //Initialize location button and setOnClickListener
        locationBtn = binding.btnLocation
        locationBtn.setOnClickListener {
            startActivity(Intent(applicationContext, MapActivity::class.java))
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
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT)
                        .show()
            }
        }

        //Initialize date and setOnClickListener
        binding.textDateEdit.inputType = InputType.TYPE_NULL
        binding.textDateEdit.isClickable=true
        binding.textDateEdit.setOnClickListener {
            reminderCalendar = GregorianCalendar.getInstance()
            DatePickerDialog(
                    this,
                    this,
                    reminderCalendar.get(Calendar.YEAR),
                    reminderCalendar.get(Calendar.MONTH),
                    reminderCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        //Get latitude and longitude
        var latitude = intent.getDoubleExtra("Latitude", 0.0)
        var longitude = intent.getDoubleExtra("Longitude", 0.0)

        //Initialize add button and setOnClickListener
        addBtn = binding.btnAdd
        addBtn.setOnClickListener {

            val reminderCalendar = GregorianCalendar.getInstance()
            val dateFormat = "dd.MM.yyyy HH:mm"

            //This can be done with API version 26
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val formatter = DateTimeFormatter.ofPattern(dateFormat)
                val date = LocalDateTime.parse(binding.textDateEdit.text, formatter)

                reminderCalendar.set(Calendar.YEAR,date.year)
                reminderCalendar.set(Calendar.MONTH,date.monthValue-1)
                reminderCalendar.set(Calendar.DAY_OF_MONTH,date.dayOfMonth)
                reminderCalendar.set(Calendar.HOUR_OF_DAY,date.hour)
                reminderCalendar.set(Calendar.MINUTE,date.minute)
            }
            //With lower apis we have to do it manually
            else {
                if(dateFormat.contains(":")){
                    //Split date and time to pieces
                    val dateParts = binding.textDateEdit.text.split(" ")
                            .toTypedArray()[0].split(".").toTypedArray()
                    val timeParts = binding.textDateEdit.text.split(" ")
                            .toTypedArray()[1].split(":").toTypedArray()

                    reminderCalendar.set(Calendar.YEAR,dateParts[2].toInt())
                    reminderCalendar.set(Calendar.MONTH,dateParts[1].toInt()-1)
                    reminderCalendar.set(Calendar.DAY_OF_MONTH,dateParts[0].toInt())
                    reminderCalendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                    reminderCalendar.set(Calendar.MINUTE, timeParts[1].toInt())

                } else{
                    //no time part
                    //convert date  string value to Date format using dd.mm.yyyy
                    // here it is assumed that date is in dd.mm.yyyy
                    val dateparts = binding.textDateEdit.text.split(".").toTypedArray()
                    reminderCalendar.set(Calendar.YEAR,dateparts[2].toInt())
                    reminderCalendar.set(Calendar.MONTH,dateparts[1].toInt()-1)
                    reminderCalendar.set(Calendar.DAY_OF_MONTH,dateparts[0].toInt())
                }
            }

            //Add reminder to Firebase database
            if (latitude != 0.0 && binding.textNameEdit.text.toString() != "" &&
                    binding.textDateEdit.text.toString() != "") {

                val database = Firebase.database(getString(R.string.firebase_db_url))
                val reference = database.getReference("User")
                var key = reference.push().key

                if (key != null) {
                    val reminder = ReminderInfo(key, binding.textNameEdit.text.toString(), latitude, longitude,
                        binding.textDateEdit.text.toString(),
                        "now", "User", false)
                    reference.child(key).setValue(reminder)

                    //Set reminder with WorkManager
                    MainActivity.setReminderWithWorkManager(applicationContext, key,
                        reminderCalendar.timeInMillis, binding.textNameEdit.text.toString())
                }
                startActivity(
                        Intent(applicationContext, MainActivity::class.java)
                )
                finish()
            }
            else {
                Toast.makeText(
                        applicationContext,
                        "Fill all the fields please.",
                        Toast.LENGTH_SHORT
                ).show()
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
        val simleDateFormat = SimpleDateFormat("dd.MM.yyyy")
        binding.textDateEdit.setText(simleDateFormat.format(reminderCalendar.time))

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
        val simleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
        binding.textDateEdit.setText(simleDateFormat.format(reminderCalendar.time))
    }
}