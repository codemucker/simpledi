package org.codemucker.klang

/**
 * Like the JVM 'Runnable', but available for all kotlin multiplatform. To be replaced by a kotlin
 * version if it ever comes out
 */
interface Startable {
    fun start()
}