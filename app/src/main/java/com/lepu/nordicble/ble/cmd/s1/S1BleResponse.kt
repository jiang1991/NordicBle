package com.lepu.nordicble.ble.cmd.s1

import com.lepu.nordicble.ble.obj.Er1DataController
import com.lepu.nordicble.utils.toShort
import com.lepu.nordicble.utils.toSignedShort
import com.lepu.nordicble.utils.toUInt
import kotlin.math.ceil
import kotlin.math.nextUp
import kotlin.math.pow
import kotlin.math.roundToInt

object S1BleResponse {

    class S1Response {
        var bytes: ByteArray
        var cmd: Int
        var pkgType: Byte
        var pkgNo: Int
        var len: Int
        var content: ByteArray

        constructor(bytes: ByteArray) {
            this.bytes = bytes
            cmd = toShort(bytes[1])
            pkgType = bytes[3]
            pkgNo = bytes[4].toInt() and 0xFF
            len = toSignedShort(bytes[5], bytes[6])
            content = bytes.copyOfRange(7, 7+len)
        }
    }

    class RtParam {
        var runStatus: Byte = 0
        var hr: Int = 0
        var recordTime: Int = 0
        var leadOn: Boolean = false
        // reserve 8

        constructor(bytes: ByteArray) {
            runStatus = bytes[0]
            hr = toSignedShort(bytes[1], bytes[2])
            recordTime = toUInt(bytes.copyOfRange(3, 7))
            leadOn = bytes[7].toInt() == 0
        }
    }

    class ScaleData {

        var subType: Byte = 0x1A.toByte()
        var vendor: Byte = 0x41.toByte()
        var measureMask: Byte
        private var precisionUnit: Byte
        var weight: Int = 0
        var resistance: Int = 0
        var crcValue: Byte
        var content: ByteArray

        constructor(bytes: ByteArray) {
            measureMask = bytes[2]
            precisionUnit = bytes[3]
            weight = toSignedShort(bytes[5], bytes[4])
            resistance = toUInt(bytes.copyOfRange(6, 10))
            crcValue = bytes[10]
            content = bytes
        }

        fun getUnit(): Int {
            return precisionUnit.toInt() and 0x0F
        }

        fun getPrecision(): Int {
            return precisionUnit.toInt() and 0xF0 shr 4
        }

        fun getWeightResult(): Double {
            return weight / 10.0.pow(getPrecision().toDouble())
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