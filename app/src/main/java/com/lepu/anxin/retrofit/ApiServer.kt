package com.lepu.anxin.retrofit

import com.blankj.utilcode.util.LogUtils
import com.lepu.anxin.retrofit.response.Register
import com.lepu.anxin.retrofit.response.Test
import com.lepu.anxin.retrofit.response.TestRes
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiServer {

    // 注册设备
    @POST("/v1/transceiver/register")
    fun register() : Observable<BaseResponse<Register>>





    companion object Init {
        val baseUrl = "http://cardiot-api.kanebay.com/"
        private var logging = HttpLoggingInterceptor(logger = {
            LogUtils.d(it)
        })
            .setLevel(HttpLoggingInterceptor.Level.BODY)
//        logging.setLevel(Level.BASIC)
        private val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        fun create(): ApiServer {
            val retorfit = Retrofit.Builder()
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .build()

            return retorfit.create(ApiServer::class.java)
        }
    }
}