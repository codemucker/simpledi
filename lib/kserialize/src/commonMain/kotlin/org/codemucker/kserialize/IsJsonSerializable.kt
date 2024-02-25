package org.codemucker.kserialize

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface IsJsonSerializable {
    fun toJson() = Json.encodeToString(this)
}