package com.lepu.nordicble.ble.cmd

import com.lepu.nordicble.ble.obj.Er1DataController
import com.lepu.nordicble.utils.toUInt

object Er1BleResponse {

    class Er1Response {
        var bytes: ByteArray
        var cmd: Int
        var pkgType: Byte
        var pkgNo: Int
        var len: Int
        var content: ByteArray

        @ExperimentalUnsignedTypes
        constructor(bytes: ByteArray) {
            this.bytes = bytes
            cmd = (bytes[1].toUInt() and 0xFFu).toInt()
            pkgType = bytes[3]
            pkgNo = (bytes[4].toUInt() and 0xFFu).toInt()
            len = toUInt(bytes.copyOfRange(5, 7))
            content = bytes.copyOfRange(7, 7+len)
        }
    }


    class RtData {
        var content: ByteArray
        var param: RtParam
        var wave: RtWave

        @ExperimentalUnsignedTypes
        constructor(bytes: ByteArray) {
            content = bytes
            param = RtParam(bytes.copyOfRange(0, 20))
            wave = RtWave(bytes.copyOfRange(20, bytes.size))
        }
    }

    class RtParam {
        var hr: Int
        var sysFlag: Byte
        var battery: Int
        var recordTime: Int = 0
        var runStatus: Byte
        var leadOn: Boolean
        // reserve 11

        @ExperimentalUnsignedTypes
        constructor(bytes: ByteArray) {
            hr = toUInt(bytes.copyOfRange(0, 2))
            sysFlag = bytes[2]
            battery = (bytes[3].toUInt() and 0xFFu).toInt()
            if (bytes[8].toUInt() and 0x02u == 0x02u) {
                recordTime = toUInt(bytes.copyOfRange(4, 8))
            }
            runStatus = bytes[8]
            leadOn = (bytes[8].toUInt() and 0x07u) != 0x07u
//            Log.d(TAG, "${bytes[8]}  lead: $leadOn")
        }
    }

    class RtWave {
        var content: ByteArray
        var len: Int
        var wave: ByteArray
        var wFs : FloatArray? = null

        @ExperimentalUnsignedTypes
        constructor(bytes: ByteArray) {
            content = bytes
            len = toUInt(bytes.copyOfRange(0, 2))
            wave = bytes.copyOfRange(2, bytes.size)
            wFs = FloatArray(len)
            for (i in 0 until len) {
                wFs!![i] = Er1DataController.byteTomV(wave[2 * i], wave[2 * i + 1])
            }
        }
    }

}