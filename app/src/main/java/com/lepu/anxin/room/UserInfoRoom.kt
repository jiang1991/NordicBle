package com.lepu.anxin.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user_table")
data class UserInfoRoom(
        var name: String = "",
        var phone: String = "",
        var gender: String = "",
        var birth: String = "",
        var height: Int? = null,
        var weight: Int? = null,
        @ColumnInfo(name = "nation_id") var nationId: String? = null,
        var city: String? = null,
        var road: String? = null
) {
    @PrimaryKey var id = 1

    override fun toString(): String {
        return """
            $name
            $phone  $gender
            $birth  $height  $weight
            $nationId
            $city $road
        """.trimIndent()
    }

}