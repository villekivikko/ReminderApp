package com.example.reminderapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.reminderapp.databinding.ActivityLoginBinding
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

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