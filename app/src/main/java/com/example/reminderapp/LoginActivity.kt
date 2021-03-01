package com.example.reminderapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.reminderapp.databinding.ActivityLoginBinding


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

            //DUMMY credentials: username=user , password=password
            if (username == getString(R.string.userName) && password == getString(R.string.password)){
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }
            else {
                Toast.makeText(
                    applicationContext,
                    "Invalid login credentials.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            //save login status

            //applicationContext.getSharedPreferences(getString(R.string.sharedPreference), Context.MODE_PRIVATE).edit().putInt("LoginStatus", 1).apply()


        }
        //Register Button
        registerBtn = binding.btnRegister
        registerBtn.setOnClickListener {
            startActivity(Intent(applicationContext, RegisterActivity::class.java))
        }
        tutorialBtn = binding.btnTutorial
        tutorialBtn.setOnClickListener {
            startActivity(Intent(applicationContext, TutorialActivity::class.java))
        }
    }
}