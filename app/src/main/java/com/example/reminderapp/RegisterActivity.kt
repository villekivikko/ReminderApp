package com.example.reminderapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.Button
import android.widget.Toast
import com.example.reminderapp.databinding.ActivityRegisterBinding

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

            startActivity(
                Intent(applicationContext, LoginActivity::class.java)
            )
        }
    }
}