package com.lepu.anxin.retrofit.response

import com.google.gson.Gson

class MonitorDetail {
    var monitorCaseId: String = ""
    var instituteId: String = ""
    var instituteName: String = ""
    var officeId: String = ""
    var officeName: String = ""
    var paymentStatus: Boolean = false
    var isAccept: Boolean = false

    /**
     *  10070001	实时监护
        10070002	暂停监护
        10070003	监护完成
     */
    var status: String = ""
    var planMonitorDuration: Int = 0


    override fun toString(): String {
        return Gson().toJson(this)
    }
}

enum class STATUS(var s: String) {
    MONITORING("10070001"),
    PAUSED("10070002"),
    FINISHED("10070003")
}