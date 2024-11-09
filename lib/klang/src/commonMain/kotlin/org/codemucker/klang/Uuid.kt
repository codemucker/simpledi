package org.codemucker.klang

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
typealias Uuid = Uuid

@OptIn(ExperimentalUuidApi::class)
fun Uuid(uuidString: String) = Uuid.Companion.parse(uuidString)

@OptIn(ExperimentalUuidApi::class)
fun Uuid() = Uuid.Companion.random()

