package com.lepu.nordicble.bean

import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.text.SimpleDateFormat
import java.util.*

/***
{"version":10,"versionName":"1.0.0","fileUrl":"https:\/\/cloud.viatomtech.com\/download\/software\/apks\/rtm_v51.apk"}
 *****/
class CheckVersionBean(
    var version: Int = 0,
    var versionName: String = "",
    var fileUrl: String = ""
)