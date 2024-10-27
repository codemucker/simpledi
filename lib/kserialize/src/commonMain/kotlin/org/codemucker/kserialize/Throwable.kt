package org.codemucker.kserialize

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

interface HasThrowableAttributes {
    fun getAttributes(): Map<String, String>
}

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@Serializable(with = DeserializedExceptionSerializer::class)
class DeserializedException(
    message: String?,
    val type: String,
    val attributes: Map<String, String>,
    val stackTrace: List<SerializedThrowable.SerializedStackTraceElement>,
    cause: DeserializedException?
) :
    Exception(message, cause) {

    init {
        printStackTrace()
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

interface ThrowableSerializerFactory {
    fun toSerializedThrowable(
        throwable: Throwable,
        attributesExtractor: ((Throwable) -> Map<String, String>)? = null
    ): SerializedThrowable {
        if (throwable is DeserializedException) {
            return throwable.toSerializedThrowable()
        }
        return SerializedThrowable(
            message = throwable.message,
            type = extractClassName(throwable),//::class.qualifiedName ?: throwable::class.simpleName ?: "unknown",
            stackTrace = extractStacktrace(throwable),

            attributes = extractThrowableAttributes(throwable, attributesExtractor),
            cause = throwable.cause?.let { toSerializedThrowable(it, attributesExtractor) }
        )
    }

    fun extractThrowableAttributes(
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

    fun extractClassName(obj: Any): String {
        return obj::class.simpleName ?: "unknown"
    }

    // platforms specific code to override and provide a custom value if availabel
    fun extractStacktrace(throwable: Throwable): List<SerializedThrowable.SerializedStackTraceElement> {
        return emptyList();
    }

    //so can have extension functions attached
    companion object {

    }
}

class DefaultThrowableSerializerFactory : ThrowableSerializerFactory

private val defaultCommonFactory = DefaultThrowableSerializerFactory()
fun getCommonDefaultSerializerFactory(): ThrowableSerializerFactory = defaultCommonFactory
expect fun getPlatformSerializerFactory(): ThrowableSerializerFactory


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

    /**
     * Attach extension functions to this
     */
    companion object {
        fun from(
            throwable: Throwable,
            attributesExtractor: ((Throwable) -> Map<String, String>)? = null
        ) = getPlatformSerializerFactory().toSerializedThrowable(throwable, attributesExtractor)
    }
}

object ThrowableSerializer : KSerializer<Throwable> {
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
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
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
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

object DeserializedExceptionSerializer : KSerializer<DeserializedException> {
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        SerialDescriptor("java.lang.Exception", SerializedThrowable.serializer().descriptor)

    override fun serialize(encoder: Encoder, value: DeserializedException) {
        encoder.encodeSerializableValue(
            SerializedThrowable.serializer(),
            SerializedThrowable.from(value)
        )
    }

    override fun deserialize(decoder: Decoder): DeserializedException {
        val serializedThrowable = decoder.decodeSerializableValue(SerializedThrowable.serializer())
        return serializedThrowable.toException()
    }
}