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

package com.nlab.reminder.core.state2

import androidx.lifecycle.viewModelScope
import com.nlab.reminder.test.genFlowExecutionDispatcher
import com.nlab.reminder.test.genFlowObserveCoroutineScope
import com.nlab.statekit.Action
import com.nlab.statekit.State
import com.nlab.statekit.Store
import com.nlab.statekit.util.buildDslEpic
import com.nlab.statekit.util.createStore
import com.nlab.testkit.once
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
internal class StoreViewModelTest {
    @Before
    fun setup() = runTest {
        Dispatchers.setMain(genFlowExecutionDispatcher(testScheduler))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ViewModel's dispatch message is passed to the Store`() = runTest {
        val inputAction = TestAction()
        val store: Store<TestAction, TestState> = mock()
        val viewModel = SimpleTestViewModel(store)
        viewModel.dispatch(inputAction)
        verify(store, once()).dispatch(inputAction)
    }

    @Test
    fun `ViewModel's uiState used store's state`() = runTest {
        val expectedState = TestState()
        val store: Store<TestAction, TestState> = mock {
            whenever(mock.state) doReturn MutableStateFlow(expectedState)
        }
        assertThat(SimpleTestViewModel(store).uiState.value, equalTo(expectedState))
    }

    @Test
    fun `ViewModel's uiState closed after 5 second's, when last subscriber unsubscribed`() = runTest {
        val flow = MutableStateFlow(TestAction())
        val viewModel = object : StoreViewModel<TestAction, TestState>() {
            override fun onCreateStore(): Store<TestAction, TestState> {
                return createStore(viewModelScope, TestState(), epic = buildDslEpic {
                    whileStateUsed { flow }
                })
            }
        }
        val awaitUntilSubscriptionCreated = async {
            flow.subscriptionCount
                .filter { it > 0 }
                .first()
        }
        val subscriptionJob = viewModel.uiState.launchIn(genFlowObserveCoroutineScope())

        awaitUntilSubscriptionCreated.join()
        subscriptionJob.cancelAndJoin()
        advanceTimeBy(5_500)

        assertThat(flow.subscriptionCount.value, equalTo(0))
    }

    private class TestAction : Action
    private class TestState : State
    private class SimpleTestViewModel(
        private val store: Store<TestAction, TestState>
    ) : StoreViewModel<TestAction, TestState>() {
        override fun onCreateStore(): Store<TestAction, TestState> {
            return store
        }
    }
}