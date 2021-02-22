package com.lepu.anxin.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserInfo::class], version = 1, exportSchema = false)
abstract class MyDatabase : RoomDatabase() {
    abstract fun UserInfoDao(): UserInfoDao
}