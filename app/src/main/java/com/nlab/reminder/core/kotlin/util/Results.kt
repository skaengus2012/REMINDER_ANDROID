/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.core.kotlin.util

/**
 * Created because Jacoco doesn't recognize Kotlin's Result with runCatching.
 * @author Doohyun
 */
inline fun <T> catching(block: () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (e: Exception) {
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

inline fun <T> Result<T>.onFailure(action: (Throwable) -> Unit) {
    if (this is Result.Failure) {
        action(throwable)
    }
}