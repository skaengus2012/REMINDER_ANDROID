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

package com.nlab.statekit.dsl.reduce

import com.nlab.statekit.dsl.TestAction
import com.nlab.statekit.dsl.TestState
import com.nlab.statekit.store.createStore
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class DslReduceFactoryKtTest {
    @Test
    fun `Given inputs and expected state, When transition store with dslReduce, Then return expected state`() = runTest {
        val inputAction = TestAction.genAction()
        val inputState = TestState.State1
        val expectedState = TestState.State3
        val store = createStore(
            coroutineScope = this,
            initState = inputState,
            reduce = DslReduce<TestAction, TestState> {
                transition {
                    if (action == inputAction && current == inputState) expectedState
                    else current
                }
            }
        )
        store.dispatch(inputAction)
        advanceUntilIdle()

        val actualState = store.state.value
        assertThat(actualState, equalTo(expectedState))
    }

    @Test
    fun `Given inputs and runner, When effect from store with dslReduce, Then runner invoked`() = runTest {
        val inputAction = TestAction.genAction()
        val inputState = TestState.genState()
        val runner: () -> Unit = mockk(relaxed = true)
        val store = createStore(
            coroutineScope = this,
            initState = inputState,
            reduce = DslReduce<TestAction, TestState> {
                effect {
                    if (action == inputAction && current == inputState) runner.invoke()
                }
            }
        )
        store.dispatch(inputAction)
        advanceUntilIdle()

        verify(exactly = 1) {
            runner.invoke()
        }
    }
}