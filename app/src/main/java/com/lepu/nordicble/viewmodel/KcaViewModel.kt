package com.lepu.nordicble.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lepu.nordicble.ble.cmd.KcaBleResponse
import com.lepu.nordicble.objs.Bluetooth

class KcaViewModel : ViewModel() {

//    // bluetooth
//    val device: MutableLiveData<Bluetooth> by lazy {
//        MutableLiveData<Bluetooth>()
//    }

    // connect
    val connect: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
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