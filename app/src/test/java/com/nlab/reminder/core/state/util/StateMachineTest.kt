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

package com.nlab.reminder.core.state.util

import com.nlab.reminder.core.state.TestEvent
import com.nlab.reminder.core.state.TestState
import com.nlab.reminder.core.state.UpdateSource
import com.nlab.reminder.test.instanceOf
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import org.hamcrest.MatcherAssert.assertThat
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StateMachineTest {
    private val scope = CoroutineScope(Dispatchers.Default)

    @Test
    fun `notify state1 when when state is StateInit and Action1 receives`() = runTest {
        val stateMachine = StateMachine<TestEvent, TestState>(scope, TestState.StateInit()) {
            updateTo { (action, old) ->
                when (action) {
                    is TestEvent.Event1 -> {
                        if (old is TestState.StateInit) TestState.State1()
                        else TestState.State2()
                    }
                    is TestEvent.Event2 -> TestState.State2()
                }
            }
        }

        stateMachine
            .send(TestEvent.Event1())
            .join()
        assertThat(
            stateMachine.state.value,
            instanceOf(TestState.State1::class)
        )
    }

    @Test
    fun `invoked testAction1, testAction2 when stateMachine send TestAction1, TestAction2`() = runTest {
        val testEvent1: (UpdateSource<TestEvent.Event1, TestState>) -> Unit = mock()
        val testEvent2: (UpdateSource<TestEvent.Event2, TestState>) -> Unit = mock()
        val stateMachine = StateMachine<TestEvent, TestState>(scope, TestState.StateInit()) {
            sideEffectBy { testEvent1(it) }
            sideEffectBy { testEvent2(it) }
        }

        repeat(2) {
            stateMachine
                .send(TestEvent.Event1())
                .join()
        }

        repeat(3) {
            stateMachine
                .send(TestEvent.Event2())
                .join()
        }
        verify(testEvent1, times(2))(any())
        verify(testEvent2, times(3))(any())
    }
}