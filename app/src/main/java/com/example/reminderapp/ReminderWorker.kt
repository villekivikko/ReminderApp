package com.example.reminderapp

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationRequest
import java.util.jar.Manifest

class ReminderWorker(appContext:Context, workerParameters: WorkerParameters) :
    Worker(appContext,workerParameters) {

    override fun doWork(): Result {
        val text = inputData.getString("message") // this comes from the reminder parameters
        val key = inputData.getString("key")
        val location_x = inputData.getDouble("location_x", 0.0)
        val location_y = inputData.getDouble("location_y", 0.0)
        MainActivity.showNotification(applicationContext,text!!, key!!, location_x, location_y)
        return   Result.success()
    }
}