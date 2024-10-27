package org.codemucker.kevent


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.reflect.KClass

/**
 * Simple events bus based on flows
 */
open class EventBus<TEvent : Any>(
    replay: Int = 0,
    extraBufferCapacity: Int = 0,
    onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
) : EventEmitter<TEvent>, EventObserver<TEvent> {

    //for emitting events
    private val _allEvents = MutableSharedFlow<TEvent>(
        replay = replay,
        extraBufferCapacity = extraBufferCapacity,
        onBufferOverflow = onBufferOverflow
    )

    //for observing events
    private val allEvents: Flow<TEvent> get() = _allEvents.asSharedFlow()

    /**
     * Be notified of any event matching the given type
     */
    override suspend fun <T : TEvent> observe(eventType: KClass<T>): Flow<T> {
        return allEvents.filterIsInstance(eventType)
    }

    /**
     * Be notified of any event matching the given type and launch in the given scope
     */
    override fun <T : TEvent> observe(
        eventType: KClass<T>,
        scope: CoroutineScope,
        block: suspend (T) -> Unit
    ): Job {
        return allEvents
            .filterIsInstance(eventType)
            .launchIn(scope)
    }

    /**
     * Push an event to this event bus. If this bus is full, then don't suspend, just return false
     *
     * Return true if the event was received, false if the buffer is full
     */
    override suspend fun tryEmit(source: Any, event: TEvent): Boolean = _allEvents.tryEmit(event)

    /**
     * Push an event to this event bus, suspending if the event buffer is full
     */
    override suspend fun emit(source: Any, event: TEvent) {
        _allEvents.emit(event)
    }

    /**
     * Return a sub event bus which only receives events of the given type. This can be used to reduce
     * the amount of events the sub event-bus needs to handle.
     */
    suspend inline fun <reified T : TEvent> to(): EventBus<T> {
        val subEvents = EventBus<T>()
        observe(T::class).onEach {
            subEvents.emit(this, it)
        }
        return subEvents
    }
}


