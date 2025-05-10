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

import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.getOrThrow
import com.nlab.reminder.core.text.UiText

/**
 * @author Doohyun
 */
fun <T> Result<T>.getOrThrowMessage(
    message: UiText? = null,
    priority: FeedbackPriority? = null
): T {
    try {
        return getOrThrow()
    } catch (e: UserMessageException) {
        throw if (message == null && priority == null) e
        else {
            val originUserMessage = e.userMessage
            UserMessageException(
                message = message ?: originUserMessage.message,
                priority = priority ?: originUserMessage.priority,
                origin = e.origin
            )
        }
    } catch (e: Throwable) {
        throw UserMessageException(message, priority, e)
    }
}

fun errorMessage(
    message: UiText? = null,
    priority: FeedbackPriority? = null,
    throwable: Throwable = IllegalStateException()
) {
    errorMessageInternal(message, priority, throwable)
}

internal fun errorMessageInternal(
    message: UiText?,
    priority: FeedbackPriority?,
    throwable: Throwable
) {
    throw UserMessageException(message, priority, throwable)
}