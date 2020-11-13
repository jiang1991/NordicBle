package com.lepu.nordicble.objs

import com.lepu.nordicble.ble.cmd.BleCRC
import com.lepu.nordicble.utils.toUInt
import kotlin.experimental.inv

object BtResponse {

    private val TAG = "vt_ble"

    var listener: ReceiveListener? = null
    fun setReceiveListener(listener: ReceiveListener) {
        BtResponse.listener = listener
    }


    @OptIn(ExperimentalUnsignedTypes::class)
    fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0xA5.toByte() || bytes[i+1] != bytes[i+2].inv()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i+5, i+7))
//            Log.d(TAG, "want bytes length: $len")
            if (i+8+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+8+len)
            if (temp.last() == BleCRC.calCRC8(temp)) {
                val bleResponse = BleResponse(temp)
//                Log.d(TAG, "get response: " + temp.toHex())
                listener?.onReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i+8+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    class BleResponse {
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
                wFs!![i] = DataController.byteTomV(wave[2*i], wave[2*i + 1])
            }
        }
    }

    interface ReceiveListener {
        fun onReceived(bleResponse: BleResponse?)
    }
}