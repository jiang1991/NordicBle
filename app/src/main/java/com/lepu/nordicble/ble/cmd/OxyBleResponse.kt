package com.lepu.nordicble.ble.cmd

import android.os.Parcelable
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.utils.ByteUtils
import com.lepu.nordicble.utils.toUInt
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject

class OxyBleResponse{

    class OxyResponse(bytes: ByteArray) {
        var no:Int
        var len: Int
        var content: ByteArray

        init {
            no = toUInt(bytes.copyOfRange(3, 5))
            len = bytes.size - 8
            content = bytes.copyOfRange(7, 7 + len)
        }
    }


    @Parcelize
    class RtWave @ExperimentalUnsignedTypes constructor(var bytes: ByteArray) : Parcelable {
        var content: ByteArray = bytes
        var spo2: Int
        var pr: Int
        var battery: Int
        var batteryState: String // 0 -> not charging; 1 -> charging; 2 -> charged
        var pi: Int
        var state: String //1-> lead on; 0-> lead off; other
        var len: Int
        var waveByte: ByteArray
        var wFs: IntArray? = null
        var wByte: ByteArray? = null

        init {
            spo2 = bytes[0].toUInt().toInt()
            pr = toUInt(bytes.copyOfRange(1, 3))
            battery = bytes[3].toUInt().toInt()
            batteryState = bytes[4].toUInt().toString()
            pi = bytes[5].toUInt().toInt()
            state = bytes[6].toUInt().toString()
            len = toUInt(bytes.copyOfRange(10, 12))
            waveByte = bytes.copyOfRange(12, 12 + len)
            wFs = IntArray(len)
            wByte = ByteArray(len)
            for (i in 0 until len) {
                var temp = ByteUtils.byte2UInt(waveByte[i])
                if (temp == 156) {
                    if (i==0) {
                        temp = ByteUtils.byte2UInt(waveByte[i+1])
                    } else if (i == len-1) {
                        temp = ByteUtils.byte2UInt(waveByte[i-1])
                    } else {
                        temp = (ByteUtils.byte2UInt(waveByte[i-1]) + ByteUtils.byte2UInt(waveByte[i+1]))/2
                    }
                }

                wFs!![i] = temp
                wByte!![i] = (100 - temp/2).toByte()
            }
        }
    }

    @Parcelize
    class OxyInfo (val bytes: ByteArray) : Parcelable {
        var region: String
        var model: String
        var hwVersion: String // hardware version
        var swVersion: String // software version
        var btlVersion: String
        var pedTar: Int
        var sn: String
        var curTime: String
//        var battery: Int
        var batteryState: String  // 0 -> not charging; 1 -> charging; 2 -> charged
        var oxiThr: Int
        var motor: String
        var mode: String
        var fileList: String

        init {
            val infoStr = JSONObject(String(bytes))
            LogUtils.d("O2 Info: $infoStr")
            region = infoStr.getString("Region")
            model = infoStr.getString("Model")
            hwVersion = infoStr.getString("HardwareVer")
            swVersion = infoStr.getString("SoftwareVer")
            btlVersion = infoStr.getString("BootloaderVer")
            pedTar = infoStr.getString("CurPedtar").toInt()
            sn = infoStr.getString("SN")
            curTime = infoStr.getString("CurTIME")
            //            battery = infoStr.getString("CurBAT").toIntOrNull() // 100%, 难解，不管
            batteryState = infoStr.getString("CurBatState")
            oxiThr = infoStr.getString("CurOxiThr").toInt()
            motor = infoStr.getString("CurMotor")
            mode = infoStr.getString("CurMode")
            fileList = infoStr.getString("FileList")
        }

    }
}