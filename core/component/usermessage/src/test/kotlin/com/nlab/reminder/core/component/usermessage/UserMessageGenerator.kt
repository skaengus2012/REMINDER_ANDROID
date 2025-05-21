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
 * @author Thalys
 */
fun genUserMessageId(rawId: Long = genLong()): UserMessageId = UserMessageId(rawId = rawId)

fun genUserMessage(
    id: UserMessageId = genUserMessageId(),
    message: UiText = genUiText(),
    priority: FeedbackPriority = FeedbackPriority.entries.random()
): UserMessage = UserMessage(
    id = id,
    message = message,
    priority = priority
)

fun genUserMessageExceptionSource(
    id: UserMessageId = genUserMessageId(),
    message: UiText? = genUiText(),
    priority: FeedbackPriority? = FeedbackPriority.entries.random()
): UserMessageExceptionSource = UserMessageExceptionSource(
    id = id,
    message = message,
    priority = priority
)