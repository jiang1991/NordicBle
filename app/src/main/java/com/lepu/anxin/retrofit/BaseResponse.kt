package com.lepu.anxin.retrofit

class BaseResponse<T> {
    val errorCode: String = ""
    val errMsg: String = ""
    val success: Boolean = false
    var data: T? = null
}