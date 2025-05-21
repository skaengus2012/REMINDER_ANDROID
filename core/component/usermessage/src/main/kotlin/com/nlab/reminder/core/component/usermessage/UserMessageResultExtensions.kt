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

package com.nlab.reminder.core.component.usermessage

import com.nlab.reminder.core.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.getOrThrow

/**
 * @author Doohyun
 */
fun <T> Result<T>.getOrThrowMessage(exceptionSource: () -> UserMessageExceptionSource): T {
    return try {
        getOrThrow()
    } catch (e: UserMessageException) {
        val source = exceptionSource()
        throw if (source.message == null && source.priority == null) e
        else {
            val originUserMessage = e.userMessage
            UserMessageException(
                source = UserMessageExceptionSource(
                    id = source.id,
                    message = source.message ?: originUserMessage.message,
                    priority = source.priority ?: originUserMessage.priority
                ),
                origin = e.origin
            )
        }
    } catch (t: Throwable) {
        errorMessage(exceptionSource(), t)
    }
}

/**
 * Jacoco test report malfunction, [ExcludeFromGeneratedTestReport]
 *
 * Problem
 * 1. If not inline: Missing branch occurs in the place of use
 * 2. Parameter Overlord: [errorMessage] function not recognized in Jacoco report
 */
@Suppress("NOTHING_TO_INLINE")
@ExcludeFromGeneratedTestReport
inline fun errorMessage(
    exceptionSource: UserMessageExceptionSource,
): Nothing = errorMessage(exceptionSource, IllegalStateException())

@Suppress("NOTHING_TO_INLINE")
@ExcludeFromGeneratedTestReport
inline fun errorMessage(
    exceptionSource: UserMessageExceptionSource,
    throwable: Throwable
): Nothing = throw UserMessageException(exceptionSource, throwable)