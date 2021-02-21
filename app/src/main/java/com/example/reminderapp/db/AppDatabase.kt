package com.example.reminderapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.reminderapp.ReminderInfo

@Database(entities = [UserInfo::class], version = 3)
abstract class AppDatabase:RoomDatabase() {
   abstract fun userDao():UserDao
}
