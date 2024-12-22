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

import com.nlab.reminder.core.component.text.UiText
import com.nlab.reminder.core.component.text.genUiText
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.translation.StringIds
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
            userMessage = UserMessage(message = genUiText(), FeedbackPriority.HIGH),
            origin = Throwable()
        )
        val result = Result.Failure<Any>(throwable)
        try {
            result.getOrThrowMessage()
        } catch (e: UserMessageException) {
            assertThat(
                e,
                sameInstance(throwable)
            )
        }
    }

    @Test
    fun `Given message, priority and failed result with user message, When getOrThrowMessage with message and priority, Then result UserMessageException with message and priority `() {
        val message = genUiText()
        val priority = FeedbackPriority.MEDIUM
        val throwable = Throwable()
        val result = Result.Failure<Any>(
            UserMessageException(
                userMessage = UserMessage(message = genUiText(), FeedbackPriority.HIGH),
                origin = throwable
            )
        )
        try {
            result.getOrThrowMessage(
                message = message,
                priority = priority
            )
        } catch (e: UserMessageException) {
            assertThat(
                e.userMessage,
                equalTo(UserMessage(message = message, priority = priority))
            )
            assertThat(
                e.origin,
                sameInstance(throwable)
            )
        }
    }

    @Test
    fun `Given message and failed result with user message, When getOrThrowMessage with message, Then result throw UserMessageException with message`() {
        val message = genUiText()
        val priority = FeedbackPriority.MEDIUM
        val throwable = Throwable()
        val result = Result.Failure<Any>(
            UserMessageException(
                userMessage = UserMessage(message = genUiText(), priority),
                origin = throwable
            )
        )
        try {
            result.getOrThrowMessage(message = message)
        } catch (e: UserMessageException) {
            assertThat(
                e.userMessage,
                equalTo(UserMessage(message = message, priority = priority))
            )
            assertThat(
                e.origin,
                sameInstance(throwable)
            )
        }
    }

    @Test
    fun `Given priority and failed result with user message, When getOrThrowMessage with message, Then result throw UserMessageException with priority`() {
        val message = genUiText()
        val priority = FeedbackPriority.MEDIUM
        val throwable = Throwable()
        val result = Result.Failure<Any>(
            UserMessageException(
                userMessage = UserMessage(message = message, FeedbackPriority.HIGH),
                origin = throwable
            )
        )
        try {
            result.getOrThrowMessage(priority = priority)
        } catch (e: UserMessageException) {
            assertThat(
                e.userMessage,
                equalTo(UserMessage(message = message, priority = priority))
            )
            assertThat(
                e.origin,
                sameInstance(throwable)
            )
        }
    }

    @Test
    fun `Given message, priority and failed result, When getOrThrowMessage with message and priority, Then result throw UserMessageException`() {
        val message = genUiText()
        val priority = FeedbackPriority.MEDIUM
        val result = Result.Failure<Any>(IllegalStateException())
        try {
            result.getOrThrowMessage(
                message = message,
                priority = priority
            )
        } catch (e: UserMessageException) {
            assertThat(
                e.userMessage,
                equalTo(UserMessage(message = message, priority = priority))
            )
        }
    }

    @Test
    fun `Given failed result, When getOrThrowMessage, Then result throw UserMessageException with default options`() {
        val result = Result.Failure<Any>(IllegalStateException())
        try {
            result.getOrThrowMessage()
        } catch (e: UserMessageException) {
            assertThat(
                e.userMessage,
                equalTo(UserMessage(message = UiText(StringIds.unknown_error), priority = FeedbackPriority.LOW))
            )
        }
    }
}