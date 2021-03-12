package com.lepu.anxin.room

import android.app.Application

class RoomRepository {

    lateinit var userDao: UserDao
    lateinit var userInfo: UserInfo

    constructor(app: Application) {
        val db: UserDatabase = UserDatabase.getDatabase(app)
    }


    fun inser(info: UserInfo) {
        UserDatabase.
    }
}