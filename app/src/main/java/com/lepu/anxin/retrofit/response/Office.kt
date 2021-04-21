package com.lepu.anxin.retrofit.response

import com.google.gson.Gson

/**
 * 返回科室信息
 */
class Office {
    var officeId: String = ""
//    var officeCode: String = ""
    var officeName: String = ""
//    var name: String = ""
//    var patientFromCode: String = ""
//    var patientFromText: String = ""
    var instituteId: String = ""
//    var instituteName: String = ""
//    var contact: String = ""
//    var phone: String = ""
//    var street: String = ""
//    var city: String = ""
//    var state: String = ""

    override fun toString(): String {
        return Gson().toJson(this)
    }
}