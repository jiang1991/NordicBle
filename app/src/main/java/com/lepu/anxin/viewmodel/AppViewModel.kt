package com.lepu.anxin.viewmodel

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.createDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.lepu.anxin.UserInfoOuterClass
import com.lepu.anxin.datastore.UserInfoSerializer
import kotlinx.android.synthetic.main.activity_user_info.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AppViewModel(app: Application): AndroidViewModel(app) {

    val userInfo: MutableLiveData<UserInfoOuterClass.UserInfo> by lazy {
        MutableLiveData<UserInfoOuterClass.UserInfo>()
    }


    private val dataStore: DataStore<UserInfoOuterClass.UserInfo> = app.createDataStore(
        fileName = "user_info.pb",
        serializer = UserInfoSerializer
    )

    /**
     * get user info from dataStore
     */
    private fun updateUserInfo() {
        GlobalScope.launch {
            dataStore.data.collect {
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
            dataStore.updateData {
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

    init {
        updateUserInfo()
    }
}