package com.lepu.anxin.room

import android.content.Context
import androidx.room.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(entities = arrayOf(UserInfoRoom::class), version = 1, exportSchema = false)
public abstract class UserDatabase : RoomDatabase(){
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null


        public val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(4)

        public fun getDatabase(context: Context): UserDatabase {

            return INSTANCE?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext,
                        UserDatabase::class.java, "user_database")
                        .build()

                INSTANCE = instance

                instance
            }
        }
    }



}