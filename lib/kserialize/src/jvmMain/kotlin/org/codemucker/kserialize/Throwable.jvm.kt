package org.codemucker.kserialize

private class JvmThrowableSerializerFactory : ThrowableSerializerFactory {
    override fun extractStacktrace(throwable: Throwable): List<SerializedThrowable.SerializedStackTraceElement> {
        return throwable.stackTrace.map {
            SerializedThrowable.SerializedStackTraceElement(
                line = it.toString(),
                lineNum = it.lineNumber,
                fileName = it.fileName,
                methodName = it.methodName,
                className = it.className,
                native = it.isNativeMethod
            )
        };
    }

    override fun extractClassName(obj: Any): String {
        return obj::class.qualifiedName ?: super.extractClassName(obj)
    }
}

private val factory = JvmThrowableSerializerFactory()

actual fun getPlatformSerializerFactory(): ThrowableSerializerFactory {
    return factory
}

fun DeserializedException.getStackTrace(): Array<StackTraceElement> {
    return stackTrace.map {
        StackTraceElement(
            it.className ?: DeserializedException.UNKNOWN,
            it.methodName ?: DeserializedException.UNKNOWN,
            it.fileName,
            it.lineNum
        )
    }.toTypedArray()
}