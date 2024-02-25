package org.codemucker.klang

typealias Url = com.eygraber.uri.Url

fun Url(urlString: String) = Url.parse(urlString)
