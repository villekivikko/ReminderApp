package com.example.reminderapp


data class ReminderInfo(
    var uid: String = "",
    var message:String= "",
    var location_x:Double = 0.0,
    var location_y:Double= 0.0,
    var reminder_time: String= "",
    var creation_time:String= "",
    var creator_id:String= "",
    var reminder_seen:Boolean = false)
