package com.example.reminderapp.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Entity(tableName = "userInfo")
data class UserInfo(
    @PrimaryKey(autoGenerate = true) var uid: Int?,
    @ColumnInfo(name="name") var name:String,
    @ColumnInfo(name="email")  var email:String,
    @ColumnInfo(name="password")  var password:String,
)