package com.lepu.anxin.utils

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

public fun makeTimeStr(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd,HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}

public fun String.md5(): String {
    val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
    return bytes.toHex()
}