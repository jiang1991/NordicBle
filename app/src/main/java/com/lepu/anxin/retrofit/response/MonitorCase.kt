package com.lepu.anxin.retrofit.response

import com.google.gson.Gson

class MonitorCase {
    var monitorCaseId = ""

    constructor(id: String) {
        this.monitorCaseId = id
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }
}