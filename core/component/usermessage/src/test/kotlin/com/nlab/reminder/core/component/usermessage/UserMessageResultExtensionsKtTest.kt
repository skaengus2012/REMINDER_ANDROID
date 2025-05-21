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
import com.nlab.reminder.core.kotlin.catching
import com.nlab.reminder.core.text.genUiText
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.instanceOf
import org.junit.Test

/**
 * @author Thalys
 */

class UserMessageResultExtensionsKtTest {
    @Test
    fun `Given success result, When getOrThrowMessage, Then result return value`() {
        val value = Any()
        val result = Result.Success(value)
        val actualValue = result.getOrThrowMessage { genUserMessageExceptionSource() }
        assertThat(actualValue, sameInstance(value))
    }

    @Test
    fun `Given result with userMessageException, null properties source, When getOrThrowMessage, Then throw origin`() {
        val originThrowable = UserMessageException(
            userMessage = genUserMessage(),
            origin = Throwable()
        )
        val actual = runAndGetUserMessageException {
            Result.Failure<Any>(originThrowable).getOrThrowMessage {
                genUserMessageExceptionSource(
                    message = null,
                    priority = null
                )
            }
        }
        assertThat(actual, sameInstance(originThrowable))
    }

    @Test
    fun `Given result with userMessageException, message, When getOrThrowMessage, Then exception has correct value`() {
        val originThrowable = UserMessageException(
            userMessage = genUserMessage(),
            origin = Throwable()
        )
        val expectedMessage = genUiText()
        val actual = runAndGetUserMessageException {
            Result.Failure<Any>(originThrowable).getOrThrowMessage {
                genUserMessageExceptionSource(
                    message = expectedMessage,
                    priority = null
                )
            }
        }
        assertThat(actual.userMessage.message, sameInstance(expectedMessage))
        assertThat(actual.userMessage.priority, sameInstance(originThrowable.userMessage.priority))
    }

    @Test
    fun `Given result with userMessageException, priority, When getOrThrowMessage, Then exception has correct value`() {
        val originThrowable = UserMessageException(
            userMessage = genUserMessage(),
            origin = Throwable()
        )
        val expectedPriority = FeedbackPriority.entries.random()
        val actual = runAndGetUserMessageException {
            Result.Failure<Any>(originThrowable).getOrThrowMessage {
                genUserMessageExceptionSource(
                    message = null,
                    priority = expectedPriority
                )
            }
        }
        assertThat(actual.userMessage.message, sameInstance(originThrowable.userMessage.message))
        assertThat(actual.userMessage.priority, sameInstance(expectedPriority))
    }

    @Test
    fun `Given result with throwable, When getOrThrowMessage, Then throw userMessageException`() {
        val originThrowable = Throwable()
        val result = Result.Failure<Any>(originThrowable)
        val id = genUserMessageId()
        val message = genUiText()
        val priority = FeedbackPriority.entries.random()
        val actual = runAndGetUserMessageException {
            result.getOrThrowMessage {
                genUserMessageExceptionSource(
                    id = id,
                    message = message,
                    priority = priority
                )
            }
        }
        assertThat(actual.userMessage, equalTo(UserMessage(id = id, message = message, priority = priority)))
        assertThat(actual.origin, equalTo(originThrowable))
    }

    @Test
    fun `Given source, When call errorMessage, Then throw exception that has userMessage and illegalStateException`() {
        val source = genUserMessageExceptionSource()
        val expectedUserMessage = UserMessageException(source, Throwable()).userMessage
        val actual = runAndGetUserMessageException { errorMessage(source) }
        assertThat(actual.userMessage, equalTo(expectedUserMessage))
        assertThat(actual.origin, instanceOf(IllegalStateException::class))
    }

    @Test
    fun `Given source and exception, When call errorMessage, Then throw exception with matching value`() {
        val source = genUserMessageExceptionSource()
        val expectedUserMessage = UserMessageException(source, Throwable()).userMessage
        val expectedThrowable = Throwable()
        val actual = runAndGetUserMessageException { errorMessage(source, expectedThrowable) }
        assertThat(actual.userMessage, equalTo(expectedUserMessage))
        assertThat(actual.origin, sameInstance(expectedThrowable))
    }
}

private fun runAndGetUserMessageException(block: () -> Unit): UserMessageException {
    return (catching(block) as Result.Failure).throwable as UserMessageException
}