package com.lepu.anxin.retrofit.post

import com.google.gson.Gson
import com.lepu.anxin.vals.*

class RegisterDeviceUser(sn: String) {

    var deviceNumber: String = sn
    val deviceModel: String = deviceModelVal
    val deviceName: String = deviceNameVal

    override fun toString(): String {
        return Gson().toJson(this)
    }
}