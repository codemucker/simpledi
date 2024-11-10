package org.codemucker.klang

interface HasThrowableAttributes {
    fun getAttributes(): Map<String, String>
}

/**
 * Find the original cause of this throwable
 */
val Throwable.rootCauseOrNull: Throwable?
    get() {
        var rootCause: Throwable? = this
        while (rootCause?.cause != null) {
            rootCause = rootCause.cause
        }
        return rootCause
    }
