package com.lepu.nordicble.utils

import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.annotation.CheckVersionType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

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