package org.codemucker.kevent


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.reflect.KClass


open class EventBus<TEvent : Any> : EventEmitter<TEvent>, EventObserver<TEvent> {

    //for emitting evens
    private val _allEvents = MutableSharedFlow<TEvent>(replay = 1)
    //for observing events
    val allEvents: Flow<TEvent> get() = _allEvents.asSharedFlow()

    override suspend fun <T : TEvent> observe(eventType: KClass<T>): Flow<T> {
        return allEvents.filter { eventType.isInstance(it) } as Flow<T>
    }

    override fun <T : TEvent> observe(
        eventType: KClass<T>,
        scope: CoroutineScope,
        block: suspend (T) -> Unit
    ): Job {
        return allEvents.filter { eventType.isInstance(it) }
            .onEach(block as (suspend (Any) -> Unit)).launchIn(scope)
    }

    override suspend fun tryEmit(source: Any, event: TEvent): Boolean = _allEvents.tryEmit(event)

    override suspend fun emit(source: Any, event: TEvent) {
        _allEvents.emit(event)
    }

    suspend inline fun <reified T : TEvent> to(): EventBus<T> {
        val subEvents = EventBus<T>()
        observe(T::class).onEach {
            subEvents.emit(this, it)
        }
        return subEvents
    }
}


