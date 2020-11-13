package com.lepu.nordicble.objs

import android.os.Parcel
import android.os.Parcelable
import com.lepu.nordicble.utils.toUInt

class Er1() : Parcelable {
    var hwV: Char? = null
    var fwV: String? = null
    var btlV: String? = null
    var branchCode: String? = null
    var fileV: Int? = null
    // reserve 2
    var deviceType: Int? = null
    var protocolV: String? = null
    var curTime: String? = null
    var protocolMaxLen: Int? = null
    // reserve 4
    var snLen: Int? = null
    var sn: String? = null


    // reserve 4

    @ExperimentalUnsignedTypes
    constructor(bytes: ByteArray) : this() {

        hwV = bytes[0].toChar()
        fwV = "${bytes[4].toUInt()}.${bytes[3].toUInt()}.${bytes[32].toUInt()}.${bytes[1].toUInt()}"
        btlV = "${bytes[8].toUInt()}.${bytes[7].toUInt()}.${bytes[6].toUInt()}.${bytes[5].toUInt()}"
        branchCode = String(bytes.copyOfRange(9, 17))
        fileV = (bytes[17].toUInt() and 0xFFu).toInt()

        deviceType = toUInt(bytes.copyOfRange(20, 22))
        protocolV = "${bytes[22].toUInt()}.${bytes[23].toUInt()}"

        val year = toUInt(bytes.copyOfRange(24, 26))
        val month = (bytes[26].toUInt() and 0xFFu).toInt()
        val day = (bytes[27].toUInt() and 0xFFu).toInt()
        val hour = (bytes[28].toUInt() and 0xFFu).toInt()
        val min = (bytes[29].toUInt() and 0xFFu).toInt()
        val second = (bytes[30].toUInt() and 0xFFu).toInt()

        curTime = "$year/$month/$day $hour:$min:$second"

        protocolMaxLen = toUInt(bytes.copyOfRange(21, 23))

        snLen = (bytes[37].toUInt() and 0xFFu).toInt()
        sn = String(bytes.copyOfRange(38, 38+snLen!!))
    }

    constructor(parcel: Parcel) : this() {
        hwV = parcel.readInt().toChar()
        fwV = parcel.readString()
        btlV = parcel.readString()
        branchCode = parcel.readString()
        fileV = parcel.readInt()
        deviceType = parcel.readInt()
        protocolV = parcel.readString()
        curTime = parcel.readString()
        protocolMaxLen = parcel.readInt()
        snLen = parcel.readInt()
        sn = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(hwV!!.toInt())
        parcel.writeString(fwV)
        parcel.writeString(btlV)
        parcel.writeString(branchCode)
        parcel.writeInt(fileV!!)
        parcel.writeInt(deviceType!!)
        parcel.writeString(protocolV)
        parcel.writeString(curTime)
        parcel.writeInt(protocolMaxLen!!)
        parcel.writeInt(snLen!!)
        parcel.writeString(sn)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Er1> {
        override fun createFromParcel(parcel: Parcel): Er1 {
            return Er1(parcel)
        }

        override fun newArray(size: Int): Array<Er1?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {

        return """
            hmV: $hwV
            fmV: $fwV
            btlV: $btlV
            branchCode: $branchCode
            fileV: $fileV
            curTime: $curTime
            sn: $sn
        """.trimIndent()
    }
}