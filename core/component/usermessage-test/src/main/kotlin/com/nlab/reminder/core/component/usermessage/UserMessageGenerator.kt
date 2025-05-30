/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

import com.nlab.reminder.core.text.UiText
import com.nlab.reminder.core.text.genUiText
import com.nlab.testkit.faker.genLong

/**
 * @author Doohyun
 */
fun genUserMessageId(): UserMessageId = UserMessageId(rawId = genLong())

fun genUserMessage(
    userMessageId: UserMessageId = genUserMessageId(),
    message: UiText = genUiText(),
    priority: FeedbackPriority = FeedbackPriority.entries.random()
): UserMessage = UserMessageFactory(generateUserMessageId = { userMessageId }).createMessage(
    message = message,
    priority = priority
)

fun genUserMessageExceptionSource(
    userMessageId: UserMessageId = genUserMessageId(),
    message: UiText? = genUiText(),
    priority: FeedbackPriority? = FeedbackPriority.entries.random()
): UserMessageExceptionSource = UserMessageFactory(generateUserMessageId = { userMessageId }).createExceptionSource(
    message = message,
    priority = priority
)

fun genUserMessageException(
    userMessageId: UserMessageId = genUserMessageId(),
    message: UiText? = genUiText(),
    priority: FeedbackPriority? = FeedbackPriority.entries.random(),
    origin: Throwable = IllegalStateException()
): UserMessageException = UserMessageException(
    source = genUserMessageExceptionSource(userMessageId, message, priority),
    origin = origin
)