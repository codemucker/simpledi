package org.codemucker.klang

import kotlinx.serialization.Serializable

interface ToException {
    fun toException(): Exception
}

typealias NothingOrFail<TError> = OkOrFail<Unit, TError>

typealias OK<TValue> = OkOrFail.Ok<TValue>
typealias Fail<TError> = OkOrFail.Fail<TError>

/**
 * Provides the ability to return a success result, or return an error, without requiring an exception to be thrown
 * back to a caller. THis allows for a few things:
 *
 * The error cases are clear and can be auto completed on
 * - a kotlin 'when' can be used to have the compiler enforce handling all conditions
 * The caller can ignore the error and throw as a normal exception if they want
 * The call does not need to generate a stack trace if an error occurs (say a validation error), thereby improving performance,
 * The result and error can easily be passed on
 *  - e.g. via method calls, or a messaging system
 * Allows for a more functional approach if required
 * Removes the need for try/catch blocks in callers which can get messy (but still allows for it)
 *
 * copied and modified from https://phauer.com/2019/sealed-classes-exceptions-kotlin/
 *
 * Different to the arrow 'Result' class to not use left/right as that's not the intention, rather the ok or fail
 * which is more intuitive
 *
 */
@Serializable
sealed class OkOrFail<out TValue, out TError>  {
    data class Ok<out TValue>(val value: TValue) : OkOrFail<TValue, Nothing>() {

        //@JvmName("mapValue")
        inline fun <T> map(block: (value:TValue) -> T): T {
            return block(this.value)
        }
    }

    class Fail<out TError>(val error: TError) : OkOrFail<Nothing, TError>(), ToException {

        //@JvmName("mapError")
        inline fun <T> map(block: (error:TError) -> T): T {
            return block(error)
        }

        override fun toException(): Exception {
            when (error) {
                is Exception -> throw error
                is ToException -> throw error.toException()
                else -> throw RuntimeException(toString())
            }
        }
    }

//    inline fun <T> map(block: (OkOrFail<TValue, TError>) -> T): T {
//        return block(this)
//    }

    inline fun <T> to(ok: (value:TValue) -> T, fail: (TError) -> T): T {
        when (this) {
            is Ok -> return ok(this.value)
            is Fail -> return fail(this.error)
        }
    }

    inline fun on(ok: (value:TValue) -> Unit, fail: (TError) -> Unit) {
        when (this) {
            is Ok -> ok(this.value)
            is Fail -> fail(this.error)
        }
    }

    fun isOk(): Boolean {
        when (this) {
            is Ok -> return true
            is Fail -> return false
        }
    }

    fun isFail() = !isOk()

    /**
     * If this is a fail response, then convert it to an exception and throw it
     *
     * Invoked {@link Fail#toException}
     */
    fun onFailThrow() {
        if (this is Fail) {
            throw toException()
        }
    }

    inline fun onFail(block: (fail: TError) -> Unit) {
        if (this is Fail) {
            block(this.error)
        }
    }

    inline fun onOk(block: (value: TValue) -> Unit): OkOrFail<TValue, TError> {
        if (this is Ok) {
            block(value)
        }
        return this
    }

    fun toKotlinResult(): Result<TValue> {
        return when (this) {
            is Ok -> Result.success(value)
            is Fail -> throw toException()
        }
    }

    fun orThrow() {
        when (this) {
            is Ok -> {}
            is Fail -> throw toException()
        }
    }

    fun getOrThrow(): TValue {
        return when (this) {
            is Ok -> value
            is Fail -> throw toException()
        }
    }

    companion object {

        fun OkReturnNothing() = Ok(Unit)
    }
}
