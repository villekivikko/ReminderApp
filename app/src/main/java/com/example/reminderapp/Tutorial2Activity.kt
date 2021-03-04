package com.example.reminderapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import com.example.reminderapp.databinding.ActivityTutorial2Binding
import com.example.reminderapp.databinding.ReminderItemBinding


class Tutorial2Activity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var binding: ActivityTutorial2Binding
    private lateinit var reminderItemBinding: ReminderItemBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorial2Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Set navigation bar item click listener
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.ic_add -> {
                    startActivity(Intent(applicationContext, Tutorial3Activity::class.java))
                    finish()
                }
            }
            true
        }

        //Array of items to be displayed
        val dummy1 = ReminderInfo("","Test",0.0, 0.0,
            "4:44", AddActivity.getCurrentTime(), "User", false)

        val dummy2 = ReminderInfo("","Test1",0.0, 0.0,
            "4:45", AddActivity.getCurrentTime(), "User", false)

        val values = arrayListOf(dummy1, dummy2)

        //Listview
        listView = binding.reminderListView

        val adaptor = ReminderAdaptor(applicationContext, values)
        listView.adapter = adaptor
    }
}