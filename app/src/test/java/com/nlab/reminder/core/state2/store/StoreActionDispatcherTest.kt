/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.core.state2.store

import com.nlab.reminder.core.state2.TestAction
import com.nlab.reminder.core.state2.TestState
import com.nlab.reminder.core.state2.UpdateSource
import com.nlab.reminder.core.state2.middleware.enhancer.Enhancer
import com.nlab.reminder.core.state2.util.buildReducer
import com.nlab.testkit.once
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * @author thalys
 */
@ExperimentalCoroutinesApi
class StoreActionDispatcherTest {
    @Test
    fun `ActionDispatcher should update state to expectedState after dispatching TestAction`() = runTest {
        val initState = TestState.State1
        val expectedState = TestState.State2
        val stateHolder = MutableStateFlow<TestState>(initState)
        val actionDispatcher = StoreActionDispatcher<TestAction, TestState>(
            stateHolder,
            reduce = buildReducer { expectedState },
            enhance = mock()
        )

        actionDispatcher.dispatch(TestAction.genAction())
        assertThat(stateHolder.value, equalTo(expectedState))
    }

    @Test
    fun `ActionDispatcher should invoke updateSourceHandle with the correct parameters, when dispatching a TestAction from State1 to State2`() = runTest {
        val input: TestAction = TestAction.genAction()
        val initState = TestState.State1
        val changedState = TestState.State2
        val enhancer: Enhancer<TestAction, TestState> = mock()
        val actionDispatcher = StoreActionDispatcher(
            state = MutableStateFlow(initState),
            reduce = buildReducer { changedState },
            enhance = enhancer
        )

        actionDispatcher.dispatch(input)
        verify(enhancer, once()).invoke(actionDispatcher, UpdateSource(input, initState))
    }
}