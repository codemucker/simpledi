package org.codemucker.klang

typealias Uri = com.eygraber.uri.Uri

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

data class UriParseError(val invalidUri: String, val exception: Exception) {
    val message get() = exception.message
}

fun com.eygraber.uri.Uri.Companion.parseOr(uriString: String): OkOrFail<Uri, UriParseError> {
    try {
        return OkOrFail.Ok(Uri.parse(uriString))
    } catch (e: Exception) {
        return OkOrFail.Fail(UriParseError(uriString, e))
    }
}