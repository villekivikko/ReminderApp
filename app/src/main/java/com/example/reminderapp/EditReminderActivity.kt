package com.example.reminderapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.reminderapp.databinding.ActivityEditReminderBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class EditReminderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditReminderBinding
    private lateinit var applyBtn: Button
    private lateinit var deleteBtn: Button
    private lateinit var locationBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditReminderBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        //Set navigation bar item click listener
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.ic_home -> {
                    startActivity(
                        Intent(applicationContext, MainActivity::class.java)
                    )
                    finish()
                }
                R.id.ic_add -> {
                    startActivity(
                        Intent(applicationContext, AddActivity::class.java)
                    )
                    finish()
                }
                R.id.ic_profile -> {
                    startActivity(
                        Intent(applicationContext, ProfileActivity::class.java)
                    )
                    finish()
                }
            }
            true
        }

        var key = intent.getStringExtra("key")
        var message = intent.getStringExtra("message")
        var location_x = intent.getDoubleExtra("latitude", 0.0)
        var location_y = intent.getDoubleExtra("longitude", 0.0)
        var latitude = intent.getDoubleExtra("latitudeNew", 0.0)
        var longitude = intent.getDoubleExtra("longitudeNew", 0.0)
        if(latitude!=0.0){
            location_x = latitude
            location_y = longitude
        }
        var time = intent.getStringExtra("time")
        var timeSplit = time?.split("/")?.toTypedArray()

        runOnUiThread {
            binding.textNameEdit.hint = message
            if(!timeSplit.isNullOrEmpty()) {
                binding.textDateEdit.hint = timeSplit[0]
                binding.textTimeEdit.hint = timeSplit[1]
            }
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
                time = binding.textDateEdit.text.toString() + "/" + binding.textTimeEdit.hint.toString()
            } else if (binding.textTimeEdit.text.toString() != "") {
                time = binding.textDateEdit.hint.toString() + "/" + binding.textTimeEdit.text.toString()
            }
            if (binding.textTimeEdit.text.toString() != "" && binding.textDateEdit.text.toString() != "") {
                time = binding.textDateEdit.text.toString() + "/" + binding.textTimeEdit.text.toString()
            }
            if(latitude!=0.0){
                location_x = latitude
                location_y = longitude
            }
            val reminder = ReminderInfo(key!!, message!!, location_x, location_y, time!!,
                    "Now", "User", false)
            //Update firebase
            val database = Firebase.database(getString(R.string.firebase_db_url))
            val reference = database.getReference("User")
            reference.child(key).setValue(reminder)

            //Return to MainActivity
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
    }
}