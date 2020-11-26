package com.lepu.nordicble.vals

import java.util.*

object EventMsgConst {

    /**
     * ble discovery
     */
    const val EventDeviceFound = "com.lepu.ble.device.found"

    /**
     * ER1 LiveDataBus Event
     */
    const val EventEr1Info = "com.lepu.ble.kac.er1.info"
    const val EventEr1RtData = "com.lepu.ble.kac.er1.rtData"

    /**
     * Oxy LiveDataBus Event
     */
    const val EventOxyInfo = "com.lepu.ble.kac.oxy.info"
    const val EventOxyRtData = "com.lepu.ble.kac.oxy.rtData"

    /**
     * KcaBle LiveDataBus event
     */
    val EventKcaSn = "com.lepu.ble.kac.sn"
    val EventKcaMeasureState = "com.lepu.ble.kac.measure.state"
    val EventKcaBpResult = "com.lepu.ble.kac.bp.result"


    /**
     * bind new device LiveDataBus
     */
    const val EventBindEr1Device = "com.lepu.ble.kac.bind.device.er1"
    const val EventBindO2Device = "com.lepu.ble.kac.bind.device.o2"
    const val EventBindKcaDevice = "com.lepu.ble.kac.bind.device.kca"


    /**
     * socket
     */
    const val EventSocketConnect = "com.lepu.socket.connect"
    const val EventSocketMsg = "com.lepu.socket.msg"
}