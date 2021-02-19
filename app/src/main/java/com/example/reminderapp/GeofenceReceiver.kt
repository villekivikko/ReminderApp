package com.example.reminderapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class GeofenceReceiver: BroadcastReceiver() {
    lateinit var key: String
    lateinit var message: String

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null){
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            val geofencingTransition = geofencingEvent.geofenceTransition

            if (geofencingTransition == Geofence.GEOFENCE_TRANSITION_ENTER||
                    geofencingTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                if (intent != null){
                    key = intent.getStringExtra("key")!!
                    message = intent.getStringExtra("message")!!
                }

                // Retrieve from firebase
                val firebase = Firebase.database
                val reference = firebase.getReference("LocationFromMap")
                val reminderListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val reminder = snapshot.getValue<Reminder>()
                        if (reminder != null){
                            MapActivity.showNotification(
                                context.applicationContext,
                                message
                            )

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("Reminder:onCancelled: ${error.details}")
                    }

                }
                val child = reference.child(key)
                child.addValueEventListener(reminderListener)
            }
        }
    }
}