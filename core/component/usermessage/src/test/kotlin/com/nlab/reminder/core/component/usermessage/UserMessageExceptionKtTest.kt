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
import com.nlab.reminder.core.translation.StringIds
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class UserMessageExceptionKtTest {
    @Test
    fun `Given filled source and throwable, When make userMessageException, Then return matching value`() {
        val source = genUserMessageExceptionSource()
        val throwable = Throwable()
        val actualUserMessageException = UserMessageException(source, throwable)
        assertThat(
            actualUserMessageException.userMessage.message,
            equalTo(source.message)
        )
        assertThat(
            actualUserMessageException.userMessage.priority,
            equalTo(source.priority)
        )
        assertThat(
            actualUserMessageException.origin,
            sameInstance(throwable)
        )
    }

    @Test
    fun `Given source without message, When make userMessageException, Then return exception with default message`() {
        val source = genUserMessageExceptionSource(message = null)
        val actualUserMessageException = UserMessageException(source, Throwable())
        assertThat(
            actualUserMessageException.userMessage.message,
            equalTo(UiText(StringIds.unknown_error))
        )
    }

    @Test
    fun `Given source without priority, When make userMessageException, Then return exception with low priority`() {
        val source = genUserMessageExceptionSource(priority = null)
        val actualUserMessageException = UserMessageException(source, Throwable())
        assertThat(
            actualUserMessageException.userMessage.priority,
            equalTo(FeedbackPriority.LOW)
        )
    }
}