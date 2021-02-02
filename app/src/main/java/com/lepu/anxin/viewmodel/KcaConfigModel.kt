package com.lepu.anxin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lepu.anxin.ble.obj.KcaBpConfig

class KcaConfigModel : ViewModel()  {

    val config: MutableLiveData<KcaBpConfig.MeasureConfig> by lazy {
        MutableLiveData<KcaBpConfig.MeasureConfig>()
    }
}