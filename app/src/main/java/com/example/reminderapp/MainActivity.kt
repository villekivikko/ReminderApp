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
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.reminderapp.databinding.ActivityMainBinding
import com.google.android.gms.location.GeofencingClient
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
    private lateinit var geofencingClient: GeofencingClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        var show = false
        setContentView(view)

        //Set top bar item click listener
        binding.topNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.ic_location -> {
                startActivity(Intent(applicationContext, VirtualLocationActivity::class.java))
                }
                R.id.ic_show -> {
                    if(!show) {
                        show = true
                    }
                    else if (show){
                        show = false
                    }
                    retrieveFirebase(show)
                }
                R.id.ic_logout -> {
                    //Cancel reminders
                    // Retrieve from firebase and cancel reminders
                    val firebase = Firebase.database
                    val user = LoginActivity.usernameGlobal
                    val reminderListener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (child in snapshot.child(user).children) {
                                val reminder = child.getValue(ReminderInfo::class.java)
                                if (reminder != null) {
                                    if (reminder.uid != "" && !reminder.reminder_seen) {
                                        cancelReminder(applicationContext, reminder.uid)
                                    }
                                    if(reminder.location_x != 0.0){
                                        geofencingClient.removeGeofences(mutableListOf(reminder.uid))
                                    }
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            println("Reminder:onCancelled: ${error.details}")
                        }
                    }
                    firebase.reference.addValueEventListener(reminderListener)
                    LoginActivity.usernameGlobal = ""
                    LoginActivity.passwordMatch = 0

                    startActivity(Intent(applicationContext, LoginActivity::class.java))
                    finish()
                }
            }
            true
        }

        //Set navigation bar item click listener
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.ic_add -> {
                    startActivity(Intent(applicationContext, AddActivity::class.java))
                    finish()
                }
                R.id.ic_profile -> {
                    startActivity(Intent(applicationContext, ProfileActivity::class.java))
                    finish()
                }
            }
            true
        }
        retrieveFirebase(show)

        listView.setOnItemClickListener{ _, _, position, _ ->
            val element :ReminderInfo= listView.getItemAtPosition(position) as ReminderInfo
            val intent = Intent(this, EditReminderActivity::class.java)
            intent.putExtra("key", element.uid)
            intent.putExtra("message", element.message)
            intent.putExtra("latitude", element.location_x)
            intent.putExtra("longitude", element.location_y)
            intent.putExtra("time", element.reminder_time)
            startActivity(intent)
            finish()
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
                for (child in snapshot.child(LoginActivity.usernameGlobal).children) {
                    val reminder = child.key?.let { snapshot.child(LoginActivity.usernameGlobal).child(it).getValue(
                        ReminderInfo::class.java) }

                    if(!show) {
                        if (reminder?.message != null && reminder.reminder_seen) {
                            values.add(reminder)
                            adaptor.notifyDataSetChanged()
                        }
                    }
                    if(show) {
                        if (reminder?.message != null) {
                            values.add(reminder)
                            adaptor.notifyDataSetChanged()
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
        var virtualLatitude = 0.0
        var virtualLongitude = 0.0
        var isLocationOffered = 0

        fun showNotification(context: Context?, message: String, key: String, location_x: Double, location_y: Double) {
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

            // Retrieve from firebase
            val firebase = Firebase.database
            val reminderListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.child(LoginActivity.usernameGlobal).children) {
                        val reminder = child.key?.let { snapshot.child(LoginActivity.usernameGlobal).child(it).getValue(
                            ReminderInfo::class.java) }

                        if (reminder?.uid == key) {
                            //Update firebase
                            reminder.reminder_seen = true

                            val database = Firebase.database("https://reminderapp-37bb2-default-rtdb.firebaseio.com")
                            val reference = database.getReference(LoginActivity.usernameGlobal)
                            reference.child(key).setValue(reminder)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Reminder:onCancelled: ${error.details}")
                }
            }

            if(location_x!=0.0) {
                println(location_x.toString() + "+" + virtualLatitude.toString())
                if ((virtualLatitude < location_x + 0.004 &&
                    virtualLatitude > location_x - 0.004) && (virtualLongitude < location_y + 0.004 &&
                            virtualLongitude > location_y - 0.004)
                ) {
                       notificationManager.notify(notificationId, notificationBuilder.build())
                }
            }
            else if(location_x == 0.0){
                notificationManager.notify(notificationId, notificationBuilder.build())
            }

            firebase.reference.addValueEventListener(reminderListener)
        }

        fun setReminderWithWorkManager(
                context: Context,
                key: String,
                timeInMillis: Long,
                message: String,
                location_x: Double,
                location_y: Double
        ) {
            val reminderParameters = Data.Builder()
                    .putString("message", message)
                    .putString("key", key)
                    .putDouble("location_x", location_x)
                    .putDouble("location_y", location_y)
                    .build()

            // get minutes from now until reminder
            var minutesFromNow = 0L
            if (timeInMillis > System.currentTimeMillis())
                minutesFromNow = timeInMillis - System.currentTimeMillis()

            val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInputData(reminderParameters)
                    .setInitialDelay(minutesFromNow, TimeUnit.MILLISECONDS)
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(key,
                    ExistingWorkPolicy.REPLACE, reminderRequest)
        }

        fun cancelReminder(context: Context, key: String){
            println("Canceled!")
            WorkManager.getInstance(context).cancelUniqueWork(key)
        }
    }
}