package com.lepu.nordicble.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lepu.nordicble.objs.Bluetooth

class MainViewModel : ViewModel() {

    val er1DeviceName: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
    val er1Bluetooth: MutableLiveData<Bluetooth> by lazy {
        MutableLiveData<Bluetooth>()
    }

    val oxyDeviceName: MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }
    val oxyBluetooth: MutableLiveData<Bluetooth> by lazy {
        MutableLiveData<Bluetooth>()
    }

    val kcaDeviceName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val kcaBluetooth: MutableLiveData<Bluetooth> by lazy {
        MutableLiveData<Bluetooth>()
    }

//    val relayId : MutableLiveData<String> by lazy {
//        MutableLiveData<String>()
//    }

    val socketState: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val hostIp : MutableLiveData<String?> by lazy {
        MutableLiveData<String?>()
    }

    val hostPort : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

}