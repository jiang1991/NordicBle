package com.lepu.anxin.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.lepu.anxin.ServerConfigOuterClass.ServerConfig
import com.lepu.anxin.UserInfoOuterClass
import java.io.InputStream
import java.io.OutputStream

object ServerConfigSerializer: Serializer<ServerConfig> {

    override val defaultValue: ServerConfig = ServerConfig.getDefaultInstance()

    override fun readFrom(input: InputStream): ServerConfig {
        try {
            return ServerConfig.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }
    override fun writeTo(t: ServerConfig, output: OutputStream) {
        t.writeTo(output)
    }
}