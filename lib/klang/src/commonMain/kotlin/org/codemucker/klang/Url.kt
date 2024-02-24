package org.codemucker.klang

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias Url =  com.eygraber.uri.Url

fun Url(urlString:String) = Url.parse(urlString)

object UrlSerializer : KSerializer<Url> {
    override val descriptor = PrimitiveSerialDescriptor("Url", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Url {
        return Url(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Url) {
        encoder.encodeString(value.toString())
    }
}