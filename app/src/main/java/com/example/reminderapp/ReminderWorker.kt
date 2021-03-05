package com.example.reminderapp

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(appContext:Context, workerParameters: WorkerParameters) :
    Worker(appContext,workerParameters) {

    override fun doWork(): Result {
        val text = inputData.getString("message") // this comes from the reminder parameters
        val key = inputData.getString("key")
        MainActivity.showNotification(applicationContext,text!!, key!!)
        return   Result.success()
    }
}