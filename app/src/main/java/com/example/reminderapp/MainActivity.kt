package com.example.reminderapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.reminderapp.databinding.ActivityMainBinding
import com.example.reminderapp.databinding.ReminderItemBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var binding: ActivityMainBinding
    private lateinit var reminderItemBinding: ReminderItemBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        var show = false
        setContentView(view)


        //Set top bar item click listener
        binding.topNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.ic_show -> {

                    if(!show) {
                        show = true
                        it.isChecked = true
                    }
                    else if (show){
                        show = false
                        it.isChecked = false
                    }
                    retrieveFirebase(show)
                }
                R.id.ic_logout -> {
                    //TODO
                }
            }
            true
        }

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

        retrieveFirebase(show)

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



    }

    private fun retrieveFirebase(show: Boolean){
        val values = arrayListOf<ReminderInfo>()
        listView = binding.reminderListView
        val adaptor = ReminderAdaptor(applicationContext, values)
        listView.adapter = adaptor

        // Retrieve from firebase
        val firebase = Firebase.database
        val reminderListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.child("User").children) {
                    val reminder = child.key?.let { snapshot.child("User").child(it).getValue(
                        ReminderInfo::class.java) }

                    if(!show) {
                        if (reminder?.message != null && reminder?.reminder_seen) {
                            values.add(reminder)
                            adaptor.notifyDataSetChanged()
                        } else {
                            println("FAILED TO RETRIEVE")
                        }
                    }
                    if(show) {
                        if (reminder?.message != null) {
                            values.add(reminder)
                            adaptor.notifyDataSetChanged()
                        } else {
                            println("FAILED TO RETRIEVE")
                        }
                    }
                }

            }


            override fun onCancelled(error: DatabaseError) {
                println("Reminder:onCancelled: ${error.details}")
            }
        }

        firebase.reference.addValueEventListener(reminderListener)
    }

    companion object {

        fun showNotification(context: Context?, message: String, key: String) {
            val CHANNEL_ID = "REMINDER_NOTIFICATION_CHANNEL"
            var notificationId = 1589
            notificationId += Random(notificationId).nextInt(1, 30)

            val notificationBuilder =
                NotificationCompat.Builder(context!!.applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_baseline_alarm_24)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(message)
                    .setStyle(
                            NotificationCompat.BigTextStyle()
                                    .bigText(message)
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setGroup(CHANNEL_ID)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.app_name)
                }
                notificationManager.createNotificationChannel(channel)
            }
            notificationManager.notify(notificationId, notificationBuilder.build())

            // Retrieve from firebase
            val firebase = Firebase.database
            val reminderListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.child("User").children) {
                        val reminder = child.key?.let { snapshot.child("User").child(it).getValue(
                            ReminderInfo::class.java) }

                        if (reminder?.uid == key) {
                            if (reminder != null) {
                                println(reminder)
                                //Update firebase
                                reminder.reminder_seen = true

                                val database = Firebase.database("https://reminderapp-37bb2-default-rtdb.firebaseio.com")
                                val reference = database.getReference("User")
                                if (key != null) {
                                    reference.child(key).setValue(reminder)
                                }
                            }
                        }
                    }

                }


                override fun onCancelled(error: DatabaseError) {
                    println("Reminder:onCancelled: ${error.details}")
                }
            }
            firebase.reference.addValueEventListener(reminderListener)


            println("******************************* " + key + " ***********************")
        }

        fun setReminderWithWorkManager(
                context: Context,
                uid: String,
                timeInMillis: Long,
                message: String
        ) {
            val reminderParameters = Data.Builder()
                    .putString("message", message)
                    .putString("uid", uid)
                    .build()

            // get minutes from now until reminder
            var minutesFromNow = 0L
            if (timeInMillis > System.currentTimeMillis())
                minutesFromNow = timeInMillis - System.currentTimeMillis()

            val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInputData(reminderParameters)
                    .setInitialDelay(minutesFromNow, TimeUnit.MILLISECONDS)
                    .build()

            WorkManager.getInstance(context).enqueue(reminderRequest)
        }

    }
}