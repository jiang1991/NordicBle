package com.lepu.anxin.retrofit

import com.blankj.utilcode.util.LogUtils
import com.lepu.anxin.retrofit.post.GetDepartmentList
import com.lepu.anxin.retrofit.post.RegisterDeviceUser
import com.lepu.anxin.retrofit.response.BaseResponse
import com.lepu.anxin.retrofit.response.DeviceRegister
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface ApiServer {

    // 2.1 注册设备
    @POST("/api/mpmonitor/v1/transceiver/register")
    fun registerRelay(
        @HeaderMap header: Map<String, String>,
        @Body registerDeviceUser: RegisterDeviceUser
    ) : Observable<BaseResponse<DeviceRegister>>

    // 2.2 记录登记患者信息
    @POST("/api/mpmonitor/v1/transceiver/record_registered_patients")
    fun recordRegisterPatient(
    ) : Observable<BaseResponse<String>>

    // 2.3 查询患者监护历史
    @POST("/api/mpmonitor/v1/transceiver/patient_monitoring_history")
    fun queryGuardHistory()
//    fun queryGuardHistory() : Observable<BaseResponse<>>

    // 2.4 上报监护事件
    @POST("/api/mpmonitor/v1/transceiver/report_monitoring_events")
    fun reportGuardEvent()

    // 3.1 获取科室列表
    @POST("/api/mpmonitor/v1/transceiver/department_list")
    fun queryDepartment(
        @HeaderMap header: Map<String, String>,
        @Body getDepartmentList: GetDepartmentList
    ) : Observable<BaseResponse<String>>

    // 3.2 登记患者信息
    @POST("/api/mpmonitor/v1/transceiver/registered_patients")
    fun registerPatient()

    // 3.3 获取监护服务器列表
    @POST("/api/mpmonitor/v1/transceiver/server_list")
    fun getServerList()

    // 3.4 查询监护详情

    // 4.1 分析报告列表

    // 4.2 获取分析报告内容

}