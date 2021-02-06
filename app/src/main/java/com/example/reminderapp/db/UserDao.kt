package com.example.reminderapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface UserDao {
    @Transaction
    @Insert
    fun insertUser(userInfo: UserInfo): Long

    @Query("DELETE FROM userInfo WHERE uid = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM userInfo")
    fun getUserInfo(): List<UserInfo>
/*
    @Query("SELECT * from userInfo where name=(:username) and password=(:password)")
    fun getUserInfo(username: String, password: String): List<UserInfo>


 */
}