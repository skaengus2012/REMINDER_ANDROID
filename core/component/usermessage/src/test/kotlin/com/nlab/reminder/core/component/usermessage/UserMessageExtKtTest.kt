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
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */

class UserMessageExtKtTest {
    @Test
    fun `Given success result, When getOrThrowMessage, Then result return value`() {
        val value = Any()
        val result = Result.Success(value)
        assertThat(result.getOrThrowMessage(), sameInstance(value))
    }

    @Test
    fun `Given failed result with user message, When getOrThrowMessage, Then result throw origin`() {
        val throwable = UserMessageException(
            message = genUiText(),
            priority = FeedbackPriority.HIGH,
            origin = Throwable()
        )

        val actual = runAndGetUserMessageException { Result.Failure<Any>(throwable).getOrThrowMessage() }
        assertThat(actual, sameInstance(throwable))
    }

    @Test
    fun `Given result with user message, When getOrThrowMessage with message and priority, Then throw matching exception`() {
        val throwable = Throwable()
        val result = Result.Failure<Any>(
            UserMessageException(
                message = genUiText(),
                priority = FeedbackPriority.HIGH,
                origin = throwable
            )
        )

        val message = genUiText()
        val priority = FeedbackPriority.URGENT
        val actual = runAndGetUserMessageException {
            result.getOrThrowMessage(message = message, priority = priority)
        }

        assertThat(
            actual.userMessage,
            equalTo(UserMessage(message = message, priority = priority))
        )
        assertThat(
            actual.origin,
            sameInstance(throwable)
        )
    }

    @Test
    fun `Given result with user message, When getOrThrowMessage with message, Then throw matching exception`() {
        val throwable = Throwable()
        val priority = FeedbackPriority.URGENT
        val result = Result.Failure<Any>(
            UserMessageException(
                message = genUiText(),
                priority = priority,
                origin = throwable
            )
        )

        val message = genUiText()
        val actual = runAndGetUserMessageException {
            result.getOrThrowMessage(message = message)
        }
        assertThat(
            actual.userMessage,
            equalTo(UserMessage(message = message, priority = priority))
        )
        assertThat(
            actual.origin,
            sameInstance(throwable)
        )
    }

    @Test
    fun `Given result with user message, When getOrThrowMessage with priority, Then throw matching exception`() {
        val message = genUiText()
        val throwable = Throwable()
        val result = Result.Failure<Any>(
            UserMessageException(
                message = message,
                priority = FeedbackPriority.HIGH,
                origin = throwable
            )
        )

        val priority = FeedbackPriority.MEDIUM
        val actual = runAndGetUserMessageException {
            result.getOrThrowMessage(priority = priority)
        }
        assertThat(
            actual.userMessage,
            equalTo(UserMessage(message = message, priority = priority))
        )
        assertThat(
            actual.origin,
            sameInstance(throwable)
        )
    }

    @Test
    fun `Given message, priority and failed result, When getOrThrowMessage with message and priority, Then result throw UserMessageException`() {
        val message = genUiText()
        val priority = FeedbackPriority.MEDIUM
        val result = Result.Failure<Any>(IllegalStateException())
        val actual = runAndGetUserMessageException {
            result.getOrThrowMessage(message = message, priority = priority)
        }
        assertThat(
            actual.userMessage,
            equalTo(UserMessage(message = message, priority = priority))
        )
    }

    @Test
    fun `Given failed result, When getOrThrowMessage, Then result throw UserMessageException with default options`() {
        val expected = UserMessageException(
            message = null,
            priority = null,
            origin = Throwable()
        )
        val result = Result.Failure<Any>(IllegalStateException())
        val actual = runAndGetUserMessageException {
            result.getOrThrowMessage()
        }
        assertThat(actual.userMessage, equalTo(expected.userMessage))
    }

    @Test
    fun `Given message, priority, throwable, When invoke internal errorMessage, Then throw matching exception`() {
        val message = genUiText()
        val priority = FeedbackPriority.URGENT
        val throwable = Throwable()
        val actual = runAndGetUserMessageException {
            errorMessageInternal(message, priority, throwable)
        }
        assertThat(actual.userMessage.message, equalTo(message))
        assertThat(actual.userMessage.priority, equalTo(priority))
        assertThat(actual.origin, sameInstance(throwable))
    }

    @Test
    fun `When invoke errorMessage without parameter, Then execute internal errorMessage with matching params`() {
        mockkStatic(::errorMessageInternal)
        every { errorMessageInternal(anyNullable(), anyNullable(), any()) } just Runs
        errorMessage()
        verify(exactly = 1) {
            errorMessageInternal(
                message = isNull(),
                priority = isNull(),
                throwable = match { it is IllegalStateException }
            )
        }
        unmockkStatic(::errorMessageInternal)
    }

    @Test
    fun `Given uiText, priority, When execute errorMessage, Then throwing matching exception`() {
        val uiText = genUiText()
        val priority = FeedbackPriority.URGENT
        val throwable = Throwable()

        mockkStatic(::errorMessageInternal)
        every { errorMessageInternal(anyNullable(), anyNullable(), any()) } just Runs

        errorMessage(uiText, priority, throwable)
        verify(exactly = 1) {
            errorMessageInternal(message = uiText, priority = priority, throwable = throwable)
        }

        unmockkStatic(::errorMessageInternal)
    }
}

private fun runAndGetUserMessageException(block: () -> Unit): UserMessageException {
    return (catching(block) as Result.Failure).throwable as UserMessageException
}