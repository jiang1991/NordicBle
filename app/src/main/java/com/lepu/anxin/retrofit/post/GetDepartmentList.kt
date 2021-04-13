package com.lepu.anxin.retrofit.post

import com.google.gson.Gson

class GetDepartmentList(id: String) {

    val doctorId = ""

    override fun toString(): String {
        return Gson().toJson(this)
    }
}