package org.codemucker.kserialize

import org.codemucker.kserialize.getCommonDefaultSerializerFactory

actual fun getPlatformSerializerFactory()  = getCommonDefaultSerializerFactory()