package org.codemucker.klang

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun nowUtc() = Clock.System.now().toLocalDateTime(TimeZone.UTC)

