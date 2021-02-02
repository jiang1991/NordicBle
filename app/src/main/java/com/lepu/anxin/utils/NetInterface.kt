package com.lepu.anxin.utils

import com.lepu.anxin.bean.CheckVersionBean
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface NetInterface {


    @GET
    fun checkVersion(@Url url: String): Observable<CheckVersionBean>?


    @Streaming
    @GET
    fun downLoadApk(@Url url: String):Observable<ResponseBody>

}


