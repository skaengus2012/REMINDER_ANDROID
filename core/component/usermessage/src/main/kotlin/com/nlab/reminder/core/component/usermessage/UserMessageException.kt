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
import com.nlab.reminder.core.text.UiText
import com.nlab.reminder.core.translation.StringIds

/**
 * @author Doohyun
 */
@ExcludeFromGeneratedTestReport
class UserMessageException(
    val userMessage: UserMessage,
    val origin: Throwable
) : RuntimeException()

internal fun UserMessageException(
    message: UiText?,
    priority: FeedbackPriority?,
    origin: Throwable
): UserMessageException = UserMessageException(
    userMessage = UserMessage(
        message = message ?: UiText(StringIds.unknown_error),
        priority = priority ?: FeedbackPriority.LOW
    ),
    origin = origin
)