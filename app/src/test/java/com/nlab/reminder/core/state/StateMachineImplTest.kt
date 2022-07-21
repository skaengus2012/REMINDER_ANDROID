/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.core.state

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class StateMachineImplTest {
    @Test
    fun `actionProcessor sent event when state machine sent`() = runTest {
        val actionProcessor: ActionProcessor<TestAction> = mock()
        val stateMachine = StateMachineImpl<TestAction, TestState>(actionProcessor, mock())
        val action = TestAction.Action1()
        stateMachine.send(action)
        verify(actionProcessor, times(1)).send(action)
    }
}