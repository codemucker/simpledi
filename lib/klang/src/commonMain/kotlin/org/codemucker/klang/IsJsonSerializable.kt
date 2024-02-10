package org.codemucker.klang

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface IsJsonSerializable {
    fun toJson() = Json.encodeToString(this)
}