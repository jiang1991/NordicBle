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
    const val EventEr1Info = "com.lepu.ble.er1.info"
    const val EventEr1RtData = "com.lepu.ble.er1.rtData"
    const val EventEr1Unbind = "com.lepu.ble.er1.unbind"

    /**
     * Oxy LiveDataBus Event
     */
    const val EventOxyInfo = "com.lepu.ble.oxy.info"
    const val EventOxyRtData = "com.lepu.ble.oxy.rtData"
    const val EventOxyUnbind = "com.lepu.ble.oxy.unbind"

    /**
     * KcaBle LiveDataBus event
     */
    const val EventKcaSn = "com.lepu.ble.kac.sn"
    const val EventKcaMeasureState = "com.lepu.ble.kac.measure.state"
    const val EventKcaBpResult = "com.lepu.ble.kac.bp.result"
    const val EventKcaUnbind = "com.lepu.ble.kac.unbind"


    /**
     * bind new device LiveDataBus
     */
    const val EventBindEr1Device = "com.lepu.ble.bind.device.er1"
    const val EventBindO2Device = "com.lepu.ble.bind.device.o2"
    const val EventBindKcaDevice = "com.lepu.ble.bind.device.kca"


    /**
     * socket
     */
    const val EventSocketConnect = "com.lepu.socket.connect"
    const val EventSocketMsg = "com.lepu.socket.msg"
}