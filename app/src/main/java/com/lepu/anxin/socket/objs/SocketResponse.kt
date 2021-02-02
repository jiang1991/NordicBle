package com.lepu.anxin.socket.objs

import com.lepu.anxin.utils.HexString.trimStr
import com.lepu.anxin.utils.toHex
import com.lepu.anxin.utils.toUInt
import java.util.*

object SocketResponse {

    class BindPatientObj {
        val pid: String
        val bed: String
        val familyName: String
        val lastName: String
        val birth: String
        val age: Int
        val height: Int
        val weight: Int
        val gender: Int

        @ExperimentalUnsignedTypes
        constructor(bytes: ByteArray) {
            pid = trimStr(String(bytes.copyOfRange(0, 32)))
            bed = trimStr(String(bytes.copyOfRange(32, 48)))
            familyName = trimStr(String(bytes.copyOfRange(48, 80)))
            lastName = trimStr(String(bytes.copyOfRange(80, 112)))
            birth = "${toUInt(bytes.copyOfRange(112, 114))}-${(bytes[114].toUInt() and 0xFFu).toInt()}-${(bytes[115].toUInt() and 0xFFu).toInt()}"
            val birthYear = (toUInt(bytes.copyOfRange(112, 114)))
            age = if (birthYear == 0) {
                0
            } else {
                Calendar.getInstance().get(Calendar.YEAR) - birthYear
            }

            height = toUInt(bytes.copyOfRange(116, 118))
            weight = toUInt(bytes.copyOfRange(118, 120))
            gender = bytes[120].toInt()

        }
    }

    class UnbindPatientObj {
        val pid: String
        val deviceId: String

        constructor(bytes: ByteArray) {
            deviceId = bytes.copyOfRange(0, 64).toHex()
            pid = bytes.copyOfRange(64, 96).toHex()
        }
    }

}