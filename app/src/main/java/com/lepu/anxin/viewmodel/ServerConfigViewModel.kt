package com.lepu.anxin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lepu.anxin.retrofit.response.Office

class ServerConfigViewModel : ViewModel() {

    val ip: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val port: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val hospitalId: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val hospitalName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val officeId: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val officeName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val departments: MutableLiveData<List<Office>> by lazy {
        MutableLiveData<List<Office>>()
    }
}