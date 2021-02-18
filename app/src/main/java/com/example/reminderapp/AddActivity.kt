package com.example.reminderapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.reminderapp.databinding.ActivityAddBinding
import com.example.reminderapp.databinding.ActivityMapBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.reminderapp.db.ReminderInfo

class AddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBinding
    private lateinit var locationBtn: Button
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


        //Add reminder to Firebase database
        val reminder = ReminderInfo(null,"Test","64", "64",
            "4:44", "now", "User", false)
        val database = Firebase.database(getString(R.string.firebase_db_url))
        val reference = database.getReference("User")
        reference.push().setValue(reminder)


    }
}