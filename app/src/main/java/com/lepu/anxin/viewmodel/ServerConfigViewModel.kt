package com.lepu.anxin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ServerConfigViewModel : ViewModel() {

    val ip: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val port: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val doctorId: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val doctorName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}