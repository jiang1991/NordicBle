package com.lepu.anxin.retrofit

import com.blankj.utilcode.util.LogUtils
import com.google.gson.Gson
import com.lepu.anxin.utils.md5
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.HashMap

object RetrofitManager {

    val APP_ID = "VMS0Y5P---dqfFlp5ipCTXag"
    val language = "zh-CN" //en-US
    val token = ""
    val SECRET = "VMS0Y5P---Z7VWkWVUtLy8T3"

    val baseUrl = "http://cardiot-api.kanebay.com/"  // 尤加利默认url
    private var logging = HttpLoggingInterceptor(logger = {
//        LogUtils.d(it)
    })
        .setLevel(HttpLoggingInterceptor.Level.BODY)

//            logging.setLevel(Level.BASIC)
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    public fun server(host: String): ApiServer = Retrofit.Builder()
        .client(client)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(host)
        .build()
        .create(ApiServer::class.java)

    private fun signature(body: Any, nonce: Int): String {
        val b1 = "$APP_ID$token$language$nonce"
        val b2 = "" // form-data
        val b3 = Gson().toJson(body)
        val b4 = SECRET

        val sign = "$b1$b2$b3$b4".md5()

//        LogUtils.d(b1, b2, b3, b4, sign)
        return sign
    }

    fun header(body: Any) :Map<String, String> {

        return HashMap<String, String>().apply {
            (0..99999).random().let {
//                this["Content-Type"] = "application/json; charset=UTF-8"
                this["appid"] = APP_ID
                this["token"] = token
                this["signature-app"] = signature(body, it)
                this["user-language"] = language
                this["nonce"] = it.toString()

                LogUtils.d("header = $this")
            }
        }
    }
}