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

package com.nlab.statekit.reduce.dsl

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import com.nlab.statekit.reduce.ActionDispatcher
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

/**
 * @author Thalys
 */
class DslReduceFactoryKtTest {
    @Test
    fun `Given action, When transition from delReduce, Then return expected state`() = runTest {
        val inputAction = TestAction.genAction()
        val inputState = TestState.State1
        val expectedState = TestState.State3

        val reduce = DslReduce<TestAction, TestState> {
            transition {
                if (action == inputAction && current == inputState) expectedState
                else current
            }
        }
        val actualState = reduce.transitionTo(inputAction, inputState)
        assertThat(actualState, equalTo(expectedState))
    }

    @Test
    fun `Given action, When launch effect from dslReduce, Then invoked action`() = runTest {
        val inputAction = TestAction.genAction()
        val inputState = TestState.genState()
        val runnable: () -> Unit = mock()

        val reduce = DslReduce<TestAction, TestState> {
            effect {
                if (action == inputAction && current == inputState) runnable.invoke()
            }
        }
        reduce.launchEffect(
            inputAction,
            inputState,
            object : ActionDispatcher<TestAction> {
                override suspend fun dispatch(action: TestAction) = Unit
            }
        )
        verify(runnable, once()).invoke()
    }
}