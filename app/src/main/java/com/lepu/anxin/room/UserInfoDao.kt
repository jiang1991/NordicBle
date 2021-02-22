package com.lepu.anxin.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserInfoDao {

    @Query("SELECT * FROM user_info order by uid DESC limit 1")
    fun lastOrNull(): UserInfo?

    @Insert
    fun insert(info: UserInfo)
}