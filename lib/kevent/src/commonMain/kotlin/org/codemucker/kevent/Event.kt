package org.codemucker.kevent

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.codemucker.klang.nowUtc
import org.codemucker.klang.Uuid
import org.codemucker.kserialize.UuidSerializer

interface Event

interface HasEventId {
    val id: Uuid
}

interface HasEventDate {
    val created: LocalDateTime
}

@Serializable
abstract class BaseEvent(
    @Serializable(with = UuidSerializer::class)
    override val id: Uuid = Uuid(),
    override val created: LocalDateTime = nowUtc()
) : Event,
    HasEventId, HasEventDate

open class NamedEvent(val eventName: String) : BaseEvent()
