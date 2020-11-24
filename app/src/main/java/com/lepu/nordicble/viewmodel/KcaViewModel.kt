package com.lepu.nordicble.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lepu.nordicble.ble.KcaBleInterface
import com.lepu.nordicble.ble.cmd.KcaBleResponse

class KcaViewModel : ViewModel() {

//    // bluetooth
//    val device: MutableLiveData<Bluetooth> by lazy {
//        MutableLiveData<Bluetooth>()
//    }

    val deviceName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    // connect
    val connect: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val battery: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    // measure
    val measureState: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val rtBp: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val bpResult: MutableLiveData<KcaBleResponse.KcaBpResult> by lazy {
        MutableLiveData<KcaBleResponse.KcaBpResult>()
    }
}