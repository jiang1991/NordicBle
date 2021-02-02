package com.lepu.anxin.utils

import com.lepu.anxin.annotation.CheckVersionType
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object NetUtils {


    /***
    https://api.viatomtech.com.cn/update/lepu/anxinbao
     *****/
    fun getCheckVersion(@CheckVersionType key: String = CheckVersionType.WIRELESS): String =
        "https://api.viatomtech.com.cn/update/lepu/${key}"


   val retrofit by lazy {
        Retrofit.Builder().baseUrl("https://www.baidu.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }





}