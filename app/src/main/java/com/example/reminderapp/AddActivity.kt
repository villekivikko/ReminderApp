package com.example.reminderapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.example.reminderapp.databinding.ActivityAddBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

const val REQUEST_CODE_SPEECH = 100

class AddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBinding
    private lateinit var locationBtn: Button
    private lateinit var addBtn: Button
    private lateinit var voiceBtn: ImageButton
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
}