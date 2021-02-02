package com.lepu.nordicble.ble.cmd

import android.os.Parcelable
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.ble.obj.Er1DataController
import com.lepu.nordicble.utils.toHex
import com.lepu.nordicble.utils.toUInt
import kotlinx.android.parcel.Parcelize
import java.util.*

object Er1BleResponse {

    @Parcelize
    class Er1Response @ExperimentalUnsignedTypes constructor(var bytes: ByteArray) : Parcelable {
        var cmd: Int
        var pkgType: Byte
        var pkgNo: Int
        var len: Int
        var content: ByteArray

        init {
            cmd = (bytes[1].toUInt() and 0xFFu).toInt()
            pkgType = bytes[3]
            pkgNo = (bytes[4].toUInt() and 0xFFu).toInt()
            len = toUInt(bytes.copyOfRange(5, 7))
            content = bytes.copyOfRange(7, 7+len)
        }
    }

    class Er3RtData constructor(var bytes: ByteArray) {
        var content: ByteArray = bytes
        var param: RtParam
        var wave: Er3RtWave

        init {
            param = RtParam(bytes.copyOfRange(0, 20))
            wave = Er3RtWave(bytes.copyOfRange(20, bytes.size))
        }
    }

    @ExperimentalUnsignedTypes
    class Er3RtWave constructor(var bytes: ByteArray) {
        var content: ByteArray = bytes
        var len: Int
        val channels = 8
        var wave : ByteArray? = null
        var wFs : FloatArray? = null

        init {
            len = toUInt(bytes.copyOfRange(0, 2))
//            LogUtils.d(len, bytes.size)
            if (len > 0) {
                wave = bytes.copyOfRange(2, bytes.size)

                wFs = FloatArray(len)

                for (i in 0 until (len * channels) step 8) {
                    wFs!![i/8] = Er1DataController.byteTomV(wave!![2 * i + 2], wave!![2 * i + 3])
                }
//                LogUtils.d(wave!!.toHex())
//                LogUtils.d(len, Arrays.toString(wFs))
            }
        }
    }


    @Parcelize
    class RtData @ExperimentalUnsignedTypes constructor(var bytes: ByteArray) : Parcelable {
        var content: ByteArray = bytes
        var param: RtParam
        var wave: RtWave

        init {
            param = RtParam(bytes.copyOfRange(0, 20))
            wave = RtWave(bytes.copyOfRange(20, bytes.size))
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class RtParam @ExperimentalUnsignedTypes constructor(var bytes: ByteArray) : Parcelable {
        var hr: Int
        var sysFlag: Byte
        var battery: Int
        var recordTime: Int = 0
        var runStatus: Byte
        var leadOn: Boolean
        // reserve 11

        init {
            hr = toUInt(bytes.copyOfRange(0, 2))
            sysFlag = bytes[2]
            battery = (bytes[3].toUInt() and 0xFFu).toInt()
            if (bytes[8].toUInt() and 0x02u == 0x02u) {
                recordTime = toUInt(bytes.copyOfRange(4, 8))
            }
            runStatus = bytes[8]
            leadOn = (bytes[8].toUInt() and 0x07u) != 0x07u
        }
    }

    @Parcelize
    class RtWave @ExperimentalUnsignedTypes constructor(var bytes: ByteArray) : Parcelable {
        var content: ByteArray = bytes
        var len: Int
        var wave: ByteArray
        var wFs : FloatArray? = null

        init {
            len = toUInt(bytes.copyOfRange(0, 2))
            wave = bytes.copyOfRange(2, bytes.size)
            wFs = FloatArray(len)
            for (i in 0 until len) {
                wFs!![i] = Er1DataController.byteTomV(wave[2 * i], wave[2 * i + 1])
            }
        }
    }

}