package org.codemucker.kevent.jvm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.codemucker.kevent.Event
import org.codemucker.kevent.EventBus
import org.codemucker.kevent.NamedEvent
import org.codemucker.kevent.observe

fun main() {
    runBlocking {
        data class MyTestsEvent(val value: String) :
            NamedEvent("org.codemucker.kevent.MyTestEvent")

        data class MyTestEvent2(val value: String) :
            NamedEvent("org.codemucker.kevent.MyTestEvent2")

        val myEvents = EventBus<Event>()

        myEvents.observe<Event>().onEach {
            println("Received Any event '${it}'")
        }
        myEvents.observe<MyTestEvent2>().onEach {
            println("Received MyTestEvent2 event '${it}', value='${it.value}'")
        }

        myEvents.observe<MyTestsEvent>().onEach {
            println("Received MyTestsEvent event '${it}', value='${it.value}'")
        }

        myEvents.observe<MyTestsEvent>(CoroutineScope(Dispatchers.IO)) {
            println("Scope: Received MyTestsEvent event '${it}', value='${it.value}'")
        }

        myEvents.observe<Event>(CoroutineScope(Dispatchers.IO)) {
            println("Scope: received any event '${it}'")
        }


        println("running push events")

        myEvents.tryEmit(this, MyTestsEvent(value = "eventValue1 A"))
        myEvents.tryEmit(this, MyTestEvent2(value = "eventValue1 B"))
        myEvents.tryEmit(this, MyTestEvent2(value = "eventValue2 A"))
        myEvents.tryEmit(this, MyTestEvent2(value = "eventValue2 B"))

        println("finished pushing events")


        myEvents.observe<MyTestsEvent>().onEach {
            println("3: Received MyTestsEvent event '${it}', value='${it.value}'")
        }

        Thread.sleep(5000)
        println("Finished running main")
    }


}