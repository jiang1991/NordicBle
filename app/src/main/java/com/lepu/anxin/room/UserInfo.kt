package com.lepu.anxin.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_info")
data class UserInfo (
    @PrimaryKey(autoGenerate = true)
    val uid: Int,
    val name: String?,
    val phone: String?,
    val gender: String?,
    val birth: String?,
    val height: Int?,
    val weight: Int?,
    @ColumnInfo(name = "nation_id")
    val nationId: String?,
    val city: String?,
    val road: String?,
)