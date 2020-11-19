package com.lepu.nordicble.ble.cmd

import com.lepu.nordicble.utils.toUInt
import org.json.JSONObject

class OxyBleResponse{

    class OxyResponse {
        var no:Int
        var len: Int
        var content: ByteArray

        constructor(bytes: ByteArray) {
            no = toUInt(bytes.copyOfRange(3, 5))
            len = bytes.size - 8
            content = bytes.copyOfRange(7, 7 + len)
        }
    }


    class RtWave {
        var content: ByteArray
        var spo2: Int
        var pr: Int
        var battery: Int
        var batteryState: String // 0 -> not charging; 1 -> charging; 2 -> charged
        var pi: Int
        var state: String //1-> lead on; 0-> lead off; other
        var len: Int
        var waveByte: ByteArray

        @ExperimentalUnsignedTypes
        constructor(bytes: ByteArray) {
            content = bytes
            spo2 = bytes[0].toUInt().toInt()
            pr = toUInt(bytes.copyOfRange(1, 3))
            battery = bytes[3].toUInt().toInt()
            batteryState = bytes[4].toUInt().toString()
            pi = bytes[5].toUInt().toInt()
            state = bytes[6].toUInt().toString()
            len = toUInt(bytes.copyOfRange(10, 12))
            waveByte = bytes.copyOfRange(12, 12 + len)
        }
    }

    class OxyInfo {
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

        constructor(bytes: ByteArray) {

            val infoStr = JSONObject(String(bytes))

            region = infoStr.getString("Region")
            model = infoStr.getString("Model")
            hwVersion = infoStr.getString("HardwareVer")
            swVersion = infoStr.getString("SoftwareVer")
            btlVersion = infoStr.getString("BootloaderVer")
            pedTar = infoStr.getString("CurPedtar").toInt()
            sn = infoStr.getString("SN")
            curTime = infoStr.getString("CurTIME")
            // 100%, 难解，不管
//            battery = infoStr.getString("CurBAT").toIntOrNull()
            batteryState = infoStr.getString("CurBatState")
            oxiThr = infoStr.getString("CurOxiThr").toInt()
            motor = infoStr.getString("CurMotor")
            mode = infoStr.getString("CurMode")
            fileList = infoStr.getString("FileList")

        }

    }
}