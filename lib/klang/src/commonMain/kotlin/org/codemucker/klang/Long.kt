package org.codemucker.klang

fun Long.formatBinarySize(): String {
    val kiloByteAsByte = 1.0 * 1024.0
    val megaByteAsByte = 1.0 * 1024.0 * 1024.0
    val gigaByteAsByte = 1.0 * 1024.0 * 1024.0 * 1024.0
    val teraByteAsByte = 1.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0
    val petaByteAsByte = 1.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0

    return when {
        this < kiloByteAsByte -> "${this.toDouble()} B"
        this >= kiloByteAsByte && this < megaByteAsByte -> "${format(this / kiloByteAsByte)} KB"
        this >= megaByteAsByte && this < gigaByteAsByte -> "${format(this / megaByteAsByte)} MB"
        this >= gigaByteAsByte && this < teraByteAsByte -> "${format(this / gigaByteAsByte)} GB"
        this >= teraByteAsByte && this < petaByteAsByte -> "${format(this / teraByteAsByte)} TB"
        else -> "Bigger than 1024 TB"
    }
}

//TODO: format nicely
private fun format(value: Double) = value.toString()
