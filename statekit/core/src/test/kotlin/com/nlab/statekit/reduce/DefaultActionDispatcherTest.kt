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

package com.nlab.statekit.reduce

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.once
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
class DefaultActionDispatcherTest {
    @Test
    fun `When action dispatched without transition, Then baseState never works`() = runTest {
        val initState = TestState.genState()
        val baseState = spy(MutableStateFlow(initState))
        val actionDispatcher = DefaultActionDispatcher(
            baseState,
            TestReduce(transition = null)
        )

        actionDispatcher.dispatch(TestAction.genAction())
        verify(baseState, never()).compareAndSet(any(), any())
        assertThat(baseState.value, equalTo(initState))
    }

    @Test
    fun `Given matching Transition, When action dispatched, Then state update to expectedState`() = runTest {
        val initState = TestState.State1
        val expectedState = TestState.State2
        val baseState = MutableStateFlow<TestState>(initState)
        val actionDispatcher = DefaultActionDispatcher(
            baseState,
            TestReduce(transition = TestTransitionNode { _, _ -> expectedState })
        )
        actionDispatcher.dispatch(TestAction.genAction())
        assertThat(baseState.value, equalTo(expectedState))
    }

    @Test
    fun `Given matching effect and transition, When action dispatched, Then effect works with initState`() = runTest {
        val runnable: (TestState) -> Unit = mock()
        val initState = TestState.State1
        val transitionState = TestState.State2
        val actionDispatcher = DefaultActionDispatcher(
            MutableStateFlow(initState),
            TestReduce(
                transition = TestTransitionNode { _, _ -> transitionState },
                effect = TestEffectNode { _, current, _ -> runnable(current) }
            )
        )
        actionDispatcher.dispatch(TestAction.genAction())
        verify(runnable, once()).invoke(initState)
    }
}