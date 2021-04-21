package com.lepu.anxin.retrofit.post

import com.google.gson.Gson
import com.lepu.anxin.UserInfoOuterClass
import com.lepu.anxin.vals.*

class RegisterUser(user: UserInfoOuterClass.UserInfo) {
    var officeId: String = officeIdVal!!
    var doctorId: String = serverConfig!!.doctorId
    var deviceSn: String = relayId
    var deviceModel: String = deviceModelVal
    var name: String = user.name
    var code: String = ""
    var mobile: String = user.phone
    var gender: String = when(user.gender) {
        "男" -> "00160001"
        "女" -> "00160002"
        else -> "00160003"
    }
    var birthday: String = user.birth
    var age: String = ""
    var height: Int = user.height
    var weight: Int = user.weight
    var idcard: String = user.nationId
    var address: String = "${user.city} ${user.road}"
    var instituteId: String = serverConfig!!.instituteId

    override fun toString(): String {
        return Gson().toJson(this)
    }
}