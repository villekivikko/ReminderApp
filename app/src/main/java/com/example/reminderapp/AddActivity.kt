package com.example.reminderapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.reminderapp.databinding.ActivityAddBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBinding
    private lateinit var locationBtn: Button
    private lateinit var addBtn: Button
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

        //Get latitude and longitude
        var latitude = intent.getDoubleExtra("Latitude", 0.0)
        var longitude = intent.getDoubleExtra("Longitude", 0.0)

        //Initialize add button and setOnClickListener
        addBtn = binding.btnAdd
        addBtn.setOnClickListener {
            //Add reminder to Firebase database
            if (latitude != 0.0 && binding.textNameEdit.text.toString() != "" &&
                    binding.textDateEdit.text.toString() != "" &&
                    binding.textTimeEdit.text.toString()!= "") {

                val database = Firebase.database(getString(R.string.firebase_db_url))
                val reference = database.getReference("User")
                var key = reference.push().key

                if (key != null) {
                    val reminder = ReminderInfo(key, binding.textNameEdit.text.toString(), latitude, longitude,
                        binding.textDateEdit.text.toString() + "/" + binding.textTimeEdit.text.toString(),
                        "now", "User", false)
                    reference.child(key).setValue(reminder)
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


}