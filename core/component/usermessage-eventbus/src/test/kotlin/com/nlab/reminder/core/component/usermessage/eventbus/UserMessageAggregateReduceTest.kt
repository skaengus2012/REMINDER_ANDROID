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
import com.nlab.statekit.test.reduce.transitionScenario
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author Thalys
 */
class UserMessageAggregateReduceTest {
    @Test
    fun `Given empty messages state, When userMessage posted, Then userMessage added`() = runTest {
        UserMessageAggregateReduce()
            .transitionScenario()
            .initState(UserMessageAggregateUiState(messages = emptyList()))
            .action(UserMessageAggregateAction.UserMessagePosted(genUserMessage()))
            .expectedStateFromInput {
                initState.copy(messages = listOf(action.message))
            }
            .verify()
    }

    @Test
    fun `Given single message state, When userMessage shown, Then userMessage removed`() = runTest {
        val userMessage = genUserMessage()
        UserMessageAggregateReduce()
            .transitionScenario()
            .initState(UserMessageAggregateUiState(listOf(userMessage)))
            .action(UserMessageAggregateAction.UserMessageShown(userMessage.id))
            .expectedStateFromInput { initState.copy(messages = emptyList()) }
            .verify()
    }
}