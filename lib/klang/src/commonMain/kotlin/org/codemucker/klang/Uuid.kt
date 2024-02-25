package org.codemucker.klang

import com.benasher44.uuid.uuidFrom

typealias Uuid = com.benasher44.uuid.Uuid

fun Uuid(uuidString:String) = uuidFrom(uuidString)
