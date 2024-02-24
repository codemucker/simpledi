package org.codemucker.ksimpledi

import org.codemucker.klang.Disposable

class JvmSimpleDiPlatformHelper : SimpleDiPlatformHelper {
    override fun <T> toStartable(obj: T): (suspend (T) -> Unit)? {
        if (obj is Runnable) {
            return { obj.run() }
        }
        return super.toStartable(obj);
    }

    /**
     * If this object can be converted to a disposable, using the platforms own disposable/closeable
     * interfaces/methods. E.g.in the JVM, this would map to 'AutoCloseable.close()'
     *
     * Return null if no disposable mapping exists
     */
    override fun toDisposable(obj: Any?): Disposable? {
        if (obj is AutoCloseable) {
            return object : Disposable {
                override fun dispose() {
                    obj.close()
                }
            }
        }
        return super.toDisposable(obj)
    }
}

private val jvmHelper: SimpleDiPlatformHelper = JvmSimpleDiPlatformHelper()

actual fun getPlatformHelper() = jvmHelper