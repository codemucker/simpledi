package org.codemucker.klang

/**
 * Like java's 'AutoCloseable'. Currently kotlin multiplatform does not have a common closeable/disposable
 */
interface Disposable {
    fun dispose()
}