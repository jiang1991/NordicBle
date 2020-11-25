package com.lepu.nordicble.ble.protocol

val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
class Utils {
    companion object {
        fun getHexUppercase(b: Byte): String {
            val sb = StringBuilder()
            val lh: Int = (b.toInt() and 0x0f)
            val fh: Int = (b.toInt() and 0xf0) shr 4
            sb.append("0x")
            sb.append(HEX_ARRAY.get(fh))
            sb.append(HEX_ARRAY.get(lh))
            return sb.toString()
        }

        fun bytesToHex(bytes: ByteArray): String {
            val hexChars = CharArray(bytes.size * 3)
            for (j in bytes.indices) {
                val v: Int = bytes[j].toInt() and 0xFF
                hexChars[j * 3] = HEX_ARRAY.get(v ushr 4)
                hexChars[j * 3 + 1] = HEX_ARRAY.get(v and 0x0F)
                hexChars[j * 3 + 2] = ','
            }
            return String(hexChars)
        }
    }
}