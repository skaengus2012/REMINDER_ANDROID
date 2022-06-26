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

package com.nlab.practice2021.core.state.util

import com.nlab.practice2021.core.state.TestAction
import com.nlab.practice2021.core.state.TestState
import com.nlab.practice2021.test.instanceOf
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
        val stateMachine = StateMachine<TestAction, TestState>(scope, TestState.StateInit()) {
            updateTo { (action, old) ->
                when (action) {
                    is TestAction.Action1 -> {
                        if (old is TestState.StateInit) TestState.State1()
                        else TestState.State2()
                    }
                    is TestAction.Action2 -> TestState.State2()
                }
            }
        }

        stateMachine
            .send(TestAction.Action1())
            .join()
        assertThat(
            stateMachine.state.value,
            instanceOf(TestState.State1::class)
        )
    }

    @Test
    fun `invoked testAction1, testAction2 when stateMachine send TestAction1, TestAction2`() = runTest {
        val testAction1: (UpdateSource<TestAction.Action1, TestState>) -> Unit = mock()
        val testAction2: (UpdateSource<TestAction.Action2, TestState>) -> Unit = mock()
        val stateMachine = StateMachine<TestAction, TestState>(scope, TestState.StateInit()) {
            withSideEffect(TestAction.Action1::class.java) { testAction1(it) }
            withSideEffect{ testAction2(it) }
        }

        repeat(2) {
            stateMachine
                .send(TestAction.Action1())
                .join()
        }

        repeat(3) {
            stateMachine
                .send(TestAction.Action2())
                .join()
        }
        verify(testAction1, times(2))(any())
        verify(testAction2, times(3))(any())
    }
}