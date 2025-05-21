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

import com.nlab.reminder.core.text.genUiText
import com.nlab.testkit.faker.shuffleAndGetFirst
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class UserMessageFactoryTest {
    @Test
    fun `Given message, priority, When create message, Then return userMessage with id`() {
        val userMessageId = genUserMessageId()
        val message = genUiText()
        val priority = FeedbackPriority.entries.shuffleAndGetFirst()
        val expectedUserMessage = UserMessage(
            id = userMessageId,
            message = message,
            priority = priority
        )
        val userMessageFactory = UserMessageFactory(generateUserMessageId = { userMessageId })
        val actualMessage = userMessageFactory.createMessage(message, priority)
        assertThat(actualMessage, equalTo(expectedUserMessage))
    }

    @Test
    fun `Given no args, When create exception source, Then return ExceptionSource with null message and priority`() {
        val userMessageId = genUserMessageId()
        val expectedExceptionSource = UserMessageExceptionSource(
            id = userMessageId,
            message = null,
            priority = null
        )
        val userMessageFactory = UserMessageFactory(generateUserMessageId = { userMessageId })
        val actualExceptionSource = userMessageFactory.createExceptionSource()
        assertThat(actualExceptionSource, equalTo(expectedExceptionSource))
    }

    @Test
    fun `Given message only, When create exception source, Then return ExceptionSource with message, null priority`() {
        val userMessageId = genUserMessageId()
        val message = genUiText()
        val expectedExceptionSource = UserMessageExceptionSource(
            id = userMessageId,
            message = message,
            priority = null
        )
        val userMessageFactory = UserMessageFactory(generateUserMessageId = { userMessageId })
        val actualExceptionSource = userMessageFactory.createExceptionSource(message = message)
        assertThat(actualExceptionSource, equalTo(expectedExceptionSource))
    }

    @Test
    fun `Given priority only, When create exception source, Then return ExceptionSource with priority, null message`() {
        val userMessageId = genUserMessageId()
        val priority = FeedbackPriority.entries.shuffleAndGetFirst()
        val expectedExceptionSource = UserMessageExceptionSource(
            id = userMessageId,
            message = null,
            priority = priority
        )
        val userMessageFactory = UserMessageFactory(generateUserMessageId = { userMessageId })
        val actualExceptionSource = userMessageFactory.createExceptionSource(priority = priority)
        assertThat(actualExceptionSource, equalTo(expectedExceptionSource))
    }

    @Test
    fun `Given message and priority, When create exception source, Then return ExceptionSource with all fields set`() {
        val userMessageId = genUserMessageId()
        val message = genUiText()
        val priority = FeedbackPriority.entries.shuffleAndGetFirst()
        val expectedExceptionSource = UserMessageExceptionSource(
            id = userMessageId,
            message = message,
            priority = priority
        )
        val userMessageFactory = UserMessageFactory(generateUserMessageId = { userMessageId })
        val actualExceptionSource = userMessageFactory.createExceptionSource(message = message, priority = priority)
        assertThat(actualExceptionSource, equalTo(expectedExceptionSource))
    }
}