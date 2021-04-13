package com.lepu.anxin.viewmodel

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.createDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.LogUtils
import com.lepu.anxin.ServerConfigOuterClass
import com.lepu.anxin.UserInfoOuterClass
import com.lepu.anxin.datastore.ServerConfigSerializer
import com.lepu.anxin.datastore.UserInfoSerializer
import com.lepu.anxin.retrofit.ApiServer
import com.lepu.anxin.retrofit.RetrofitManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AppViewModel(app: Application): AndroidViewModel(app) {

    val userInfo: MutableLiveData<UserInfoOuterClass.UserInfo> by lazy {
        MutableLiveData<UserInfoOuterClass.UserInfo>()
    }

    val serverConfig: MutableLiveData<ServerConfigOuterClass.ServerConfig> by lazy {
        MutableLiveData<ServerConfigOuterClass.ServerConfig>()
    }

    private val userDataStore: DataStore<UserInfoOuterClass.UserInfo> = app.createDataStore(
        fileName = "user_info.pb",
        serializer = UserInfoSerializer
    )
    private val serverDataStore: DataStore<ServerConfigOuterClass.ServerConfig> = app.createDataStore(
        fileName = "server_config.pb",
        serializer = ServerConfigSerializer
    )

    /**
     * get user info from dataStore
     */
    private fun readUserInfo() {
        GlobalScope.launch {
            userDataStore.data.collect {
                userInfo.postValue(it)
            }
        }
    }

    /**
     * save user info
     */
    fun saveUserInfo(name: String,
                     phone: String,
                     gender: String,
                     birth: String,
                     height: Int,
                     weight: Int,
                     nation_id: String?,
                     city: String?,
                     road: String?
    ) {
        GlobalScope.launch {
            userDataStore.updateData {
                it.toBuilder()
                    .setName(name)
                    .setPhone(phone)
                    .setGender(gender)
                    .setBirth(birth)
                    .setHeight(height)
                    .setWeight(weight)
                    .setNationId(nation_id)
                    .setCity(city)
                    .setRoad(road)
                    .build()
            }
            LogUtils.d("saved userInfo dataStore")
        }
    }
    /**
     * save user monitor case id
     */
    fun saveUserCaseId(id: String) {
        GlobalScope.launch { userDataStore.updateData {
            it.toBuilder()
                .setCaseId(id)
                .build()
        } }
    }

    /**
     * get server config from dataStore
     */
    private fun readServerConfig() {
        GlobalScope.launch {
            serverDataStore.data.collect {
                serverConfig.postValue(it)
            }
        }
    }

    /**
     * save server config
     */
    fun saveServerConfig(
        host: String,
        port: String,
        doctorId: String,
        doctorName: String
    ) {
        GlobalScope.launch {
            serverDataStore.updateData {
                it.toBuilder()
                    .setHost(host)
                    .setPort(port)
                    .setDoctorId(doctorId)
                    .setDoctorName(doctorName)
                    .build()
            }
            LogUtils.d("save serverConfig dataStore")
        }
    }
    /**
     * save device user id
     */
    fun saveDeviceUserId(id: String) {
        GlobalScope.launch {
            serverDataStore.updateData {
                it.toBuilder()
                    .setDeviceId(id)
                    .build()
            }
        }
    }


    /**
     * Retrofit
     */
    lateinit var server: ApiServer
    fun initServer(host: String) {
        server = RetrofitManager.server(host)
    }

    init {
        readUserInfo()
        readServerConfig()
    }
}