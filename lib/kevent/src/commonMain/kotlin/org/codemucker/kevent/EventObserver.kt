package org.codemucker.kevent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

interface EventObserver<in TEvent : Any> {
    suspend fun <T : TEvent> observe(eventType: KClass<T>): Flow<T>
    fun <T : TEvent> observe(
        eventType: KClass<T>,
        scope: CoroutineScope,
        block: suspend (T) -> Unit
    ): Job
}

suspend inline fun <reified T : Any> EventObserver<T>.observe(): Flow<T> = this.observe(T::class)

/**
 * Observe the given the event using the provided scope
 */
inline fun <reified T : Any> EventObserver<T>.observe(
    scope: CoroutineScope,
    noinline block: suspend (T) -> Unit
): Job {
    return this.observe(T::class, scope, block)
}
