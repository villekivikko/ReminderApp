package com.example.reminderapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.reminderapp.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.textUsername.text = LoginActivity.usernameGlobal

        //Set navigation bar item click listener
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.ic_home -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                }
                R.id.ic_add -> {
                    startActivity(Intent(applicationContext, AddActivity::class.java))
                    finish()
                }
            }
            true
        }
    }
}