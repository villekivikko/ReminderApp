package com.example.reminderapp.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "reminderInfo")
data class ReminderInfo(
    @PrimaryKey(autoGenerate = true) var uid: Int? = 0,
    @ColumnInfo(name="message") var message:String = "",
    @ColumnInfo(name="location_x")  var location_x:Double = 0.0,
    @ColumnInfo(name="location_y")  var location_y:Double = 0.0,
    @ColumnInfo(name="reminder_time") var reminder_time: String = "",
    @ColumnInfo(name="creation_time") var creation_time:String= "",
    @ColumnInfo(name="creator_id") var creator_id:String= "",
    @ColumnInfo(name="reminder_seen") var reminder_seen:Boolean = false

)