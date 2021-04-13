package com.lepu.anxin.retrofit.post

import com.google.gson.Gson

class RegisterDeviceUser(sn: String) {

    var deviceNumber: String = sn
    val deviceModel: String = "A1"
    val deviceName: String = "安心宝"

    override fun toString(): String {
        return Gson().toJson(this)
    }
}