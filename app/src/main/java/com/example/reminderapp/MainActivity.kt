package com.example.reminderapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import com.example.reminderapp.databinding.ActivityMainBinding
import com.example.reminderapp.databinding.ReminderItemBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


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

        val values = arrayListOf<ReminderInfo>()
        listView = binding.reminderListView
        val adaptor = ReminderAdaptor(applicationContext, values)
        listView.adapter = adaptor

        listView.setOnItemClickListener{parent, view, position, id ->
            val element :ReminderInfo= listView.getItemAtPosition(position) as ReminderInfo
            val intent = Intent(this, EditReminderActivity::class.java)
            intent.putExtra("key", element.uid)
            intent.putExtra("message", element.message)
            intent.putExtra("latitude", element.location_x)
            intent.putExtra("longitude", element.location_y)
            intent.putExtra("time", element.reminder_time)
            startActivity(intent)
        }

        // Retrieve from firebase
        val firebase = Firebase.database
        val reminderListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.child("User").children) {
                    val reminder = child.key?.let { snapshot.child("User").child(it).getValue(
                        ReminderInfo::class.java) }

                    if (reminder?.message != null) {
                        values.add(reminder)
                        adaptor.notifyDataSetChanged()
                    } else {
                        println("FAILED TO RETRIEVE")
                    }
                }

            }


            override fun onCancelled(error: DatabaseError) {
                println("Reminder:onCancelled: ${error.details}")
            }
        }
        firebase.reference.addValueEventListener(reminderListener)
    }
}