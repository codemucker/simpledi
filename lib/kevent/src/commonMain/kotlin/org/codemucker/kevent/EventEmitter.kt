package org.codemucker.kevent

interface EventEmitter<in TEvent : Any> {
    suspend fun emit(source: Any, event: TEvent) {
        tryEmit(source, event)
    }

    suspend fun tryEmit(source: Any, event: TEvent): Boolean
}

/**
 * Push events directly to an event bus
 */
class EventEmitterDelegate<in TEvent : Event>(
    private val source: Any,
    private val delegateEmitter: EventEmitter<TEvent>,
) : EventEmitter<TEvent> {

    override suspend fun tryEmit(source: Any, event: TEvent): Boolean =
        delegateEmitter.tryEmit(source, event)

    override suspend fun emit(source: Any, event: TEvent) {
        delegateEmitter.emit(source, event)
    }
}

/**
 * An emitter which does nothing
 */
class NullEventEmitter<in TEvent : Event> : EventEmitter<TEvent> {

    override suspend fun tryEmit(source: Any, event: TEvent): Boolean = true

    override suspend fun emit(source: Any, event: TEvent) {
    }
}
