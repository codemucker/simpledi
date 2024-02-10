package org.codemucker.klang

import kotlinx.datetime.*

fun nowUtc() = Clock.System.now().toLocalDateTime(TimeZone.UTC)

