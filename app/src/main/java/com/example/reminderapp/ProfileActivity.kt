package com.example.reminderapp

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.reminderapp.databinding.ActivityProfileBinding
import com.example.reminderapp.db.AppDatabase

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    //private lateinit var user: UserInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Set navigation bar item click listener
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
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
            }
            true
        }
        AsyncTask.execute {
            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "com.example.reminderapp"
            ).build()
            val user = db.userDao()
            val thisUser = user.getUserInfo()
            db.close()

            runOnUiThread {
                binding.textReminder.text = thisUser.first().name
            }
        }
    }
}