package com.lepu.anxin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserInfoViewModel: ViewModel() {

    val name: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val phone: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val gender: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val birth: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val height: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val weight: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val nationId: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val city: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val road: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}