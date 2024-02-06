package org.codemucker.klang

import kotlinx.datetime.*

fun LocalDateTime.tookTo(other: LocalDateTime, timezone: TimeZone = TimeZone.UTC): DateTimePeriod {
    val thisInstant = this.toInstant(timezone)
    val otherInstant = other.toInstant(timezone)

    //val timezone = appState.timezone
    return thisInstant.periodUntil(otherInstant, timezone)
}

fun LocalDateTime.tookToString(other: LocalDateTime, timezone: TimeZone = TimeZone.UTC): String {
    val took = this.tookTo(other, timezone)
    if (took.days > 0) {
        return "${took.days} days, ${took.hours} hours, ${took.minutes} min"
    }

    if (took.hours > 0) {
        return "${took.hours} hours, ${took.minutes} min"
    }
    if (took.minutes > 0) {
        return "${took.minutes} min, ${took.seconds} sec"
    }

    return "${took.seconds} sec"
}