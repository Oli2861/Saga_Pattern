package com.oli.plugins

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.sql.Timestamp

object TimestampAsLongSerializer: KSerializer<Timestamp> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Timestamp", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Timestamp) {
        val long: Long = value.time
        encoder.encodeLong(long)
    }

    override fun deserialize(decoder: Decoder): Timestamp {
        val long = decoder.decodeLong()
        return Timestamp(long)
    }
}
