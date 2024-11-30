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

package com.nlab.statekit.store

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import com.nlab.statekit.reduce.ActionDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
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
class ComponentStoreImplTest {
    @Test
    fun `When store dispatched, Then actionDispatcher should be dispatched`() = runTest {
        val input = TestAction.genAction()
        val actionDispatcher: ActionDispatcher<TestAction> = mock()
        val store = ComponentStoreImpl<TestAction, TestState>(
            state = mock(),
            actionDispatcher = actionDispatcher,
        )
        store.dispatch(input)
        verify(actionDispatcher, once()).dispatch(input)
    }

    @Test
    fun `Given init state, When store created, Then store has init state`() = runTest {
        val expectedState = TestState.genState()
        val store = ComponentStoreImpl<TestAction, TestState>(
            state = MutableStateFlow(expectedState),
            actionDispatcher = mock(),
        )
        assertThat(store.state.value, equalTo(expectedState))
    }
}