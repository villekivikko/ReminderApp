package com.example.reminderapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.reminderapp.databinding.ActivityLoginBinding
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {
    private lateinit var loginBtn: Button
    private lateinit var registerBtn: Button
    private lateinit var tutorialBtn: Button
    private lateinit var binding: ActivityLoginBinding
    private lateinit var geofencingClient: GeofencingClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        geofencingClient = LocationServices.getGeofencingClient(this)

        //Initialize login button and setOnClickListener
        loginBtn = binding.btnLogin
        loginBtn.setOnClickListener {
            val username = binding.txtUsername.text.toString()
            val password = binding.txtPassword.text.toString()

            // Retrieve from firebase
            val firebase = Firebase.database
            val reminderListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.child("UserDatabase").children) {
                        val user = child.key?.let {
                            snapshot.child("UserDatabase").child(it).getValue(
                                UserInfo::class.java)}
                        if (user?.username == username && user.password == password) {
                            usernameGlobal = username
                            passwordMatch = 1

                            // Retrieve from firebase and set up reminders
                            val reminderListener = object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (childReminder in snapshot.child(username).children) {
                                        val reminder = childReminder.getValue(ReminderInfo::class.java)
                                        if (reminder != null) {
                                            if (reminder.uid != "" &&
                                                    !reminder.reminder_seen &&
                                                    reminder.reminder_time != "") {
                                                MainActivity.setReminderWithWorkManager(applicationContext,
                                                        reminder.uid,
                                                        AddActivity.getTimeInCalendar(reminder.reminder_time).timeInMillis,
                                                        reminder.message,
                                                        reminder.location_x, reminder.location_y)
                                            }
                                            if (reminder.location_x != 0.0 && !reminder.reminder_seen){
                                                AddActivity.createGeoFence(applicationContext,
                                                LatLng(reminder.location_x, reminder.location_y),
                                                reminder.uid, reminder.message, geofencingClient)
                                            }
                                        }

                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    println("Reminder:onCancelled: ${error.details}")
                                }
                            }
                            firebase.reference.addValueEventListener(reminderListener)

                            startActivity(Intent(applicationContext, MainActivity::class.java))
                            finish()
                        }
                    }
                    if (passwordMatch!=1) {
                        Toast.makeText(
                            applicationContext,
                            "Invalid login credentials.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Reminder:onCancelled: ${error.details}")
                }
            }

            firebase.reference.addValueEventListener(reminderListener)

            //return@setOnClickListener

            //save login status

            //applicationContext.getSharedPreferences(getString(R.string.sharedPreference), Context.MODE_PRIVATE).edit().putInt("LoginStatus", 1).apply()


        }
        //Register Button
        registerBtn = binding.btnRegister
        registerBtn.setOnClickListener {
            startActivity(Intent(applicationContext, RegisterActivity::class.java))
            finish()
        }
        tutorialBtn = binding.btnTutorial
        tutorialBtn.setOnClickListener {
            startActivity(Intent(applicationContext, TutorialActivity::class.java))
            finish()
        }
    }

    companion object{
        var usernameGlobal = ""
        var passwordMatch = 0
    }

}