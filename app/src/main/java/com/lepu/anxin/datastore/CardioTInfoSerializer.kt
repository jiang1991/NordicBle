package com.lepu.anxin.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.lepu.anxin.CardioTInfoOuterClass
import java.io.InputStream
import java.io.OutputStream

object CardioTInfoSerializer: Serializer<CardioTInfoOuterClass.CardioTInfo> {

    override val defaultValue: CardioTInfoOuterClass.CardioTInfo = CardioTInfoOuterClass.CardioTInfo.getDefaultInstance()

    override fun readFrom(input: InputStream): CardioTInfoOuterClass.CardioTInfo {
        try {
            return CardioTInfoOuterClass.CardioTInfo.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override fun writeTo(t: CardioTInfoOuterClass.CardioTInfo, output: OutputStream) {
        t.writeTo(output)
    }
}