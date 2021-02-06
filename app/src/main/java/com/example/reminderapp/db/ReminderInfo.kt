package com.example.reminderapp.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "reminderInfo")
data class ReminderInfo(
    @PrimaryKey(autoGenerate = true) var uid: Int?,
    @ColumnInfo(name="name") var name:String,
    @ColumnInfo(name="location")  var location:String,
    @ColumnInfo(name="date") var date:String,
    @ColumnInfo(name="time") var time: String
)