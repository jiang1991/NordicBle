package com.lepu.nordicble.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class S1ViewModel  : ViewModel() {
    val deviceName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val connectState: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val runningState: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val weight: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>()
    }
    val weightUnit: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val weightPrecision: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val hrMeasureTime: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val hrMeasureValue: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
}