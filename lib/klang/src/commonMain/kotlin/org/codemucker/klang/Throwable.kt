package org.codemucker.klang

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.PrintWriter

interface HasThrowableAttributes {
    fun getAttributes(): Map<String, String>
}

@Serializable(with = ExceptionSerializer::class)
class DeserializedException(
    message: String?,
    val type: String,
    val attributes: Map<String, String>,
    val stackTrace: List<SerializedThrowable.SerializedStackTraceElement>,
    cause: DeserializedException?
) :
    Exception(message, cause) {

    override fun getStackTrace(): Array<StackTraceElement> {
        return stackTrace.map {
            StackTraceElement(
                it.className ?: UNKNOWN,
                it.methodName ?: UNKNOWN,
                it.fileName,
                it.lineNum
            )
        }.toTypedArray()
    }

    override fun toString(): String {
        val className = "DeserializedException(${type})"
        val sb = StringBuilder()
        sb.append(if (message != null) "$className: $message" else className)
        for (att in attributes) {
            sb.append("\n")
            sb.append("\t${att.key}: ${att.value}")
        }
        return sb.toString()
    }

    override fun printStackTrace(pw: PrintWriter) {
        for (ele in stackTrace) {
            pw.write(" at " + ele)
            pw.println()
        }
    }

    fun toSerializedThrowable(): SerializedThrowable {
        return SerializedThrowable(
            message = message,
            type = type,
            attributes = attributes,
            stackTrace = stackTrace,
            cause = (cause as DeserializedException?)?.toSerializedThrowable()
        )
    }

    companion object {
        val UNKNOWN = "unknown"
    }
}

@Serializable
class Foo {

}
@Serializable
data class SerializedThrowable(
    val message: String?,
    val type: String?,
    val attributes: Map<String, String> = mapOf(),
    val stackTrace: List<SerializedStackTraceElement> = listOf(),
    val cause: SerializedThrowable? = null,
) {

    @Serializable
    data class SerializedStackTraceElement(
        val line: String,
        val lineNum: Int,
        val fileName: String?,
        val className: String?,
        val methodName: String?,
        val native: Boolean
    ) {
        override fun toString(): String {
            //follow the native java implementation as much as possible
            //though handle other language cases too
            val sb = StringBuilder()
            sb.append(className ?: DeserializedException.UNKNOWN).append('.')
                .append(methodName ?: DeserializedException.UNKNOWN).append('(')
            if (native) {
                sb.append("Native Method")
            } else if (fileName == null) {
                sb.append("Unknown Source")
            } else {
                sb.append(fileName)
                if (lineNum >= 0) {
                    sb.append(':').append(lineNum)
                }
            }
            sb.append(')')

            return sb.toString()
        }
    }

    fun toException(): DeserializedException = DeserializedException(
        message = message,
        attributes = attributes,
        type = type ?: DeserializedException.UNKNOWN,
        stackTrace = stackTrace,
        cause = this.cause?.toException()
    )

    companion object {
        fun from(
            throwable: Throwable,
            attributesExtractor: ((Throwable) -> Map<String, String>)? = null
        ): SerializedThrowable {
            if (throwable is DeserializedException) {
                return throwable.toSerializedThrowable()
            }
            return SerializedThrowable(
                message = throwable.message,
                type = throwable::class.qualifiedName ?: throwable::class.simpleName ?: "unknown",
                stackTrace = throwable.stackTrace.map {
                    SerializedStackTraceElement(
                        line = it.toString(),
                        lineNum = it.lineNumber,
                        fileName = it.fileName,
                        methodName = it.methodName,
                        className = it.className,
                        native = it.isNativeMethod
                    )
                },
                attributes = extractAttributes(throwable, attributesExtractor),
                cause = throwable.cause?.let { from(it, attributesExtractor) }
            )
        }

        private fun extractAttributes(
            t: Throwable,
            attributesExtractor: ((Throwable) -> Map<String, String>)?
        ): Map<String, String> {
            if (attributesExtractor != null) {
                return attributesExtractor.invoke(t)
            }
            if (t is HasThrowableAttributes) {
                return t.getAttributes()
            }
            return mapOf()
        }
    }
}

object ThrowableSerializer : KSerializer<Throwable> {
    override val descriptor: SerialDescriptor =
        SerialDescriptor("java.lang.Throwable", SerializedThrowable.serializer().descriptor)

    override fun serialize(encoder: Encoder, value: Throwable) {
        encoder.encodeSerializableValue(
            SerializedThrowable.serializer(),
            SerializedThrowable.from(value)
        )
    }

    override fun deserialize(decoder: Decoder): Throwable {
        val serializedThrowable = decoder.decodeSerializableValue(SerializedThrowable.serializer())
        return serializedThrowable.toException()
    }
}

object ExceptionSerializer : KSerializer<Exception> {
    override val descriptor: SerialDescriptor =
        SerialDescriptor("java.lang.Exception", SerializedThrowable.serializer().descriptor)

    override fun serialize(encoder: Encoder, value: Exception) {
        encoder.encodeSerializableValue(
            SerializedThrowable.serializer(),
            SerializedThrowable.from(value)
        )
    }

    override fun deserialize(decoder: Decoder): Exception {
        val serializedThrowable = decoder.decodeSerializableValue(SerializedThrowable.serializer())
        return serializedThrowable.toException()
    }
}