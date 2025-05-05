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
import com.nlab.reminder.core.translation.StringIds

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
                userMessage = UserMessage(
                    message = message ?: originUserMessage.message,
                    priority = priority ?: originUserMessage.priority
                ),
                origin = e.origin
            )
        }
    } catch (e: Throwable) {
        throw UserMessageException(
            userMessage = UserMessage(
                message = message ?: UiText(StringIds.unknown_error),
                priority = priority ?: FeedbackPriority.LOW
            ),
            origin = e
        )
    }
}

@Suppress("NOTHING_TO_INLINE") // Application as an inline due to omission of jacoco coverage
inline fun errorMessage(
    message: UiText = UiText(StringIds.unknown_error),
    priority: FeedbackPriority = FeedbackPriority.LOW,
    origin: Throwable = IllegalStateException()
) {
    throw UserMessageException(
        userMessage = UserMessage(
            message = message,
            priority = priority
        ),
        origin = origin
    )
}