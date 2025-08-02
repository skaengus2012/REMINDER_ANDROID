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

package com.nlab.reminder.core.component.usermessage.eventbus

import com.nlab.reminder.core.component.usermessage.genUserMessage
import com.nlab.statekit.test.reduce.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author Thalys
 */
class UserMessageAggregateReduceTest {
    @Test
    fun `Given empty messages state, When userMessage posted, Then userMessage added`() = runTest {
        UserMessageAggregateReduce().test()
            .givenCurrent(UserMessageAggregateUiState(messages = emptyList()))
            .actionToDispatch(UserMessageAggregateAction.UserMessagePosted(genUserMessage()))
            .transitionScenario()
            .expectedNextState { current.copy(messages = listOf(action.message)) }
    }

    @Test
    fun `Given single message state, When userMessage shown, Then userMessage removed`() = runTest {
        val userMessage = genUserMessage()
        UserMessageAggregateReduce().test()
            .givenCurrent(UserMessageAggregateUiState(listOf(userMessage)))
            .actionToDispatch(UserMessageAggregateAction.UserMessageShown(userMessage.id))
            .transitionScenario()
            .expectedNextState { current.copy(messages = emptyList()) }
    }
}