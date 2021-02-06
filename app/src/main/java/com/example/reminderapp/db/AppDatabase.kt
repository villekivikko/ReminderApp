package com.example.reminderapp.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ReminderInfo::class, UserInfo::class], version = 3)
abstract class AppDatabase:RoomDatabase() {
   abstract fun reminderDao():ReminderDao
   abstract fun userDao():UserDao
}
