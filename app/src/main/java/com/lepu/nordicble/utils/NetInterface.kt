package com.lepu.nordicble.utils

import com.lepu.nordicble.bean.CheckVersionBean
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.text.SimpleDateFormat
import java.util.*

interface NetInterface {


    @GET
    fun checkVersion(@Url url: String): Observable<CheckVersionBean>?


    @Streaming
    @GET
    fun downLoadApk(@Url url: String):Observable<ResponseBody>

}


