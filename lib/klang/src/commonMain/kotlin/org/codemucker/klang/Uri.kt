package org.codemucker.klang

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias Uri =  com.eygraber.uri.Uri

fun Uri.isFtp(): Boolean = scheme == "ftp"
fun Uri.isHttp(): Boolean = scheme == "http" || scheme == "https"
fun Uri.isLdap(): Boolean = scheme == "ldap" || scheme == "ldaps"
fun Uri.isMailto(): Boolean = scheme == "mailto"
fun Uri.isSecure(): Boolean =
    isSecureHttp() || isSecureWebsocket() || isSecureFtp() || isSsh() || isSecureLdap()

fun Uri.isSecureFtp(): Boolean = scheme == "sftp"
fun Uri.isSecureHttp(): Boolean = scheme == "https"
fun Uri.isSecureLdap(): Boolean = scheme == "ldaps"
fun Uri.isSecureWebsocket(): Boolean = scheme == "wss"
fun Uri.isSsh(): Boolean = scheme == "ssh"
fun Uri.isUrl(): Boolean = isFtp() || isHttp() || isWebsocket()
fun Uri.isUrn(): Boolean = scheme == "urn"
fun Uri.isWebsocket(): Boolean = scheme == "ws" || scheme == "wss"

object UriSerializer : KSerializer<Uri> {
    override val descriptor = PrimitiveSerialDescriptor("Uri", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Uri {
        return Uri.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Uri) {
        encoder.encodeString(value.toString())
    }
}