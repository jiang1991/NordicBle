package com.lepu.nordicble.utils

fun toSignedShort(b1: Byte, b2: Byte): Int {
    return ((b1.toInt() and 0xff) + (b2.toInt() and 0xff shl 8))
}

fun toShort(b1: Byte) = b1.toInt() and 0xff