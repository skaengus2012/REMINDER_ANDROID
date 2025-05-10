/*
 * Copyright (C) 2024 The N's lab Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nlab.reminder.core.kotlin

/**
 * Replication of Kotlin Result
 *
 * Because Kotlin's Result is a value class, jacoco doesn't work properly.
 * So, I remastered it in the form of a data class.
 * @author Doohyun
 */
sealed class Result<out T> {
    data class Failure<T>(val throwable: Throwable) : Result<T>()
    data class Success<T>(val value: T) : Result<T>()
}

/**
 * Created because Jacoco doesn't recognize Kotlin's Result with runCatching.
 * @author Doohyun
 */
inline fun <T> catching(block: () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (e: Throwable) {
        Result.Failure(e)
    }

val <T> Result<T>.isSuccess get() = when (this) {
    is Result.Success -> true
    is Result.Failure -> false
}

val <T> Result<T>.isFailure get() = when (this) {
    is Result.Success -> false
    is Result.Failure -> true
}

fun <T> Result<T>.getOrThrow(): T = when (this) {
    is Result.Success -> value
    is Result.Failure -> throw throwable
}

fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> value
    is Result.Failure -> null
}

inline fun <T> Result<out T>.getOrElse(defaultValue: (Throwable) -> T): T = when (this) {
    is Result.Success -> value
    is Result.Failure -> defaultValue(throwable)
}

inline fun <T, U> Result<T>.map(transform: (T) -> U): Result<U> = when (this) {
    is Result.Success -> Result.Success(transform(value))
    is Result.Failure -> Result.Failure(throwable)
}

inline fun <T, U> Result<T>.flatMap(transform: (T) -> Result<U>): Result<U> = when (this) {
    is Result.Success -> transform(value)
    is Result.Failure -> Result.Failure(throwable)
}

inline fun <T> Result<T>.onFailure(action: (Throwable) -> Unit): Result<T> {
    if (this is Result.Failure) {
        action(throwable)
    }

    return this
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(value)
    }

    return this
}