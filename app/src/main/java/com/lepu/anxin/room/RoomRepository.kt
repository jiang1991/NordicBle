package com.lepu.anxin.room

import android.app.Application

class RoomRepository {

    lateinit var userDao: UserDao
    lateinit var userInfoRoom: UserInfoRoom

    constructor(app: Application) {
        val db: UserDatabase = UserDatabase.getDatabase(app)

        userDao = db.userDao()
    }


    fun inser(info: UserInfoRoom) {
        UserDatabase.databaseWriteExecutor.execute {
            userDao.insert(info)
        }
    }
}