package com.lepu.nordicble.const

import java.util.*

object BleConst {
    public val write_uuid =
        UUID.fromString("8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3")
    public val notify_uuid =
        UUID.fromString("0734594A-A8E7-4B1A-A6B1-CD5243059A57")


    /**
     * KcaBle LiveDataBus event
     */
    public val EventKcaBleConnect = "com.lepu.ble.kac.connect"
    public val EventKcaBleMeasureState = "com.lepu.ble.kac.measure.state"
    public val EventKcaBleRtBp = "com.lepu.ble.kac.rt.bp"
    public val EventKcaBleBpResult = "com.lepu.ble.kac.bp.result"


    /**
     * bind new device LiveDataBus
     */
    public const val EventBindEr1Device = "com.lepu.ble.kac.bind.device.er1"
    public const val EventBindO2Device = "com.lepu.ble.kac.bind.device.o2"
    public const val EventBindKcaDevice = "com.lepu.ble.kac.bind.device.kca"
    public const val EventBindS1ScaleDevice = "com.lepu.ble.kac.bind.device.s1"
}