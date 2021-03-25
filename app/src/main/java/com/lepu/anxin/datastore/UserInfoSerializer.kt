package com.lepu.anxin.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.lepu.anxin.UserInfoOuterClass.UserInfo
import java.io.InputStream
import java.io.OutputStream
import java.util.*

object UserInfoSerializer: Serializer<UserInfo> {

    override val defaultValue: UserInfo = UserInfo.getDefaultInstance()

    override fun readFrom(input: InputStream): UserInfo {
        try {
            return UserInfo.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override fun writeTo(t: UserInfo, output: OutputStream) {
        t.writeTo(output)
    }
}