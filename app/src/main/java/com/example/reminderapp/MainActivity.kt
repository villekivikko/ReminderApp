package com.example.reminderapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import androidx.room.Room.databaseBuilder
import com.example.reminderapp.databinding.ActivityMainBinding
import com.example.reminderapp.databinding.ReminderItemBinding
import com.example.reminderapp.db.AppDatabase
import com.example.reminderapp.db.ReminderInfo


class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var binding: ActivityMainBinding
    private lateinit var reminderItemBinding: ReminderItemBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Set navigation bar item click listener
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.ic_add -> {
                    startActivity(
                        Intent(applicationContext, AddActivity::class.java)
                    )
                    finish()
                }
                R.id.ic_profile -> {
                    startActivity(
                        Intent(applicationContext, ProfileActivity::class.java)
                    )
                    finish()
                }
            }
            true
        }

        //Array of items to be displayed
        val dummy1 = ReminderInfo(
            null,
            name = "TestName1",
            location = "TestLocation1",
            date = "1.1.2021",
            time = "4:44"
        )

        val dummy2 = ReminderInfo(
            null,
            name = "TestName2",
            location = "TestLocation2",
            date = "2.1.2021",
            time = "4:45"
        )

        val values = arrayListOf(dummy1, dummy2)

        //Listview
        listView = binding.reminderListView

        val db = databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            getString(R.string.sharedPreference)
        )
            .build()

        val adaptor = ReminderAdaptor(applicationContext, values)
        listView.adapter = adaptor

    }

}