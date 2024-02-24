package org.codemucker.klang

/**
 * An async version which can optionally join the current suspendable
 */
interface AsyncDisposable : Disposable{
    suspend fun dispose(join:Boolean) {
        dispose()
    }
}