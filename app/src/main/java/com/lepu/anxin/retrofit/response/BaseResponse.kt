package com.lepu.anxin.retrofit.response

class BaseResponse<T> {
    /**
     * {
     * "code": 0,
     * "data": "string",
     * "message": "string",
     * "reason": "string"
     * }
     */

    var code: Int = 0
    var message: String = ""
    var reason: String = ""
    var data: T? = null
}

fun BaseResponse<*>.isSuccess(): Boolean {
    return code == 1
}