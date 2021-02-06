package com.example.reminderapp

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import com.example.reminderapp.databinding.ActivityRegisterBinding
import com.example.reminderapp.db.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var registerBtn: Button
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        registerBtn = binding.btnRegister
        registerBtn.setOnClickListener {
            if (binding.txtUsername.text.isEmpty()) {
                Toast.makeText(applicationContext,
                        "Input an username please",
                        Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (binding.txtPassword.text.isEmpty()) {
                Toast.makeText(applicationContext,
                    "Input a password please",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val userInfo = UserInfo(null,
                name = binding.txtUsername.text.toString(),
                email = binding.txtEmail.text.toString(),
                password = binding.txtPassword.text.toString()
            )

            AsyncTask.execute {
                //Save userinfo to database
                val db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "com.example.reminderapp"
                ).build()
                db.userDao().insertUser(userInfo)
                db.close()
            }

            startActivity(
                Intent(applicationContext, LoginActivity::class.java)
            )
        }
    }
}