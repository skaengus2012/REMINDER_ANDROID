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

package com.nlab.reminder.core.component.usermessage.eventbus

import app.cash.turbine.test
import com.nlab.reminder.core.component.usermessage.UserMessage
import com.nlab.reminder.core.component.usermessage.genUserMessage
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class UserMessagePostedFlowTest {
    @Test
    fun `Given monitor with message, When collect flow, Then received user message posted action`() = runTest {
        val userMessage = genUserMessage()
        val userMessageMonitor: UserMessageMonitor = mockk {
            val channel = Channel<UserMessage>(capacity = 1).apply {
                send(userMessage)
            }
            every { message } returns channel
        }

        val flow = UserMessagePostedFlow(userMessageMonitor = userMessageMonitor)

        flow.test {
            assertThat(
                awaitItem(),
                equalTo(UserMessageAggregateAction.UserMessagePosted(userMessage))
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}