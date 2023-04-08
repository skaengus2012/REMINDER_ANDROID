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

import com.nlab.reminder.core.state2.*
import com.nlab.reminder.core.state2.middleware.enhancer.ActionDispatcher
import com.nlab.reminder.core.state2.middleware.enhancer.Enhancer
import com.nlab.reminder.core.state2.middleware.epic.Epic
import com.nlab.reminder.core.state2.middleware.epic.EpicSource
import com.nlab.reminder.core.state2.middleware.epic.SubscriptionStrategy
import com.nlab.reminder.core.state2.util.*
import com.nlab.testkit.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author thalys
 */
@ExperimentalCoroutinesApi
class DefaultStoreFactoryTest {
    private fun createStoreFromDefaultStoreFactory(
        coroutineScope: CoroutineScope,
        initState: TestState = TestState.genState(),
        reducer: Reducer<TestAction, TestState> = mock(),
        enhancer: Enhancer<TestAction, TestState> = mock(),
        epicSourceLoader: EpicSourceLoader = mock(),
        epic: Epic<TestAction> = mock()
    ): Store<TestAction, TestState> =
        DefaultStoreFactory(epicSourceLoader).createStore(coroutineScope, initState, reducer, enhancer, epic)

    @Test
    fun `Store was created with initValue`() = runTest {
        val expectedInitState = TestState.genState()
        val store = createStoreFromDefaultStoreFactory(coroutineScope = this, initState = expectedInitState)

        assertThat(store.state.value, equalTo(expectedInitState))
    }

    @Test
    fun `Reducer modifies state of store`() = runTest {
        val initState: TestState = TestState.State1
        val expectedState: TestState = TestState.State2
        val action = TestAction.genAction()
        val store = createStoreFromDefaultStoreFactory(
            coroutineScope = this,
            initState = initState,
            reducer = buildDslReducer {
                filteredAction(predicate = { it == action }) {
                    anyState { expectedState }
                }
            }
        )
        val storeChangeDeferred = async {
            store.state
                .filter { it == expectedState }
                .first()
        }
        store.dispatch(action).join()

        assertThat(storeChangeDeferred.await(), equalTo(expectedState))
    }

    @Test
    fun `Enhancer modified state of store`() = runTest {
        val initState: TestState = TestState.State1
        val expectedState: TestState = TestState.State2
        val input: TestAction = TestAction.Action1
        val actionInEnhancer: TestAction = TestAction.Action2
        val store = createStoreFromDefaultStoreFactory(
            coroutineScope = this,
            initState = initState,
            reducer = buildDslReducer {
                filteredAction(predicate = { it == actionInEnhancer }) {
                    anyState { expectedState }
                }
            },
            enhancer = buildDslEnhancer {
                filteredState(predicate = { it == initState }) {
                    filteredAction(predicate = { it == input }) { dispatch(actionInEnhancer) }
                }
            }
        )
        val storeChangeDeferred = async {
            store.state
                .filter { it == expectedState }
                .first()
        }
        store.dispatch(input).join()
        assertThat(storeChangeDeferred.await(), equalTo(expectedState))
    }

    @Test
    fun `Epic was fetched, when DefaultStore created`() = runTest {
        var isInvoked = false
        val inputEpicSources = List(genIntGreaterThanZero()) {
            EpicSource(
                emptyFlow<TestAction>(),
                SubscriptionStrategy.WhileStateUsed
            )
        }
        val epicSourceLoader: EpicSourceLoader = object : EpicSourceLoader {
            override fun <A : Action, S : State> load(
                coroutineScope: CoroutineScope,
                epicSources: List<EpicSource<A>>,
                actionDispatcher: ActionDispatcher<A>,
                stateFlow: MutableStateFlow<S>
            ) {
                if (epicSources == inputEpicSources) {
                    isInvoked = true
                }
            }
        }
        createStoreFromDefaultStoreFactory(
            coroutineScope = this,
            epicSourceLoader = epicSourceLoader,
            epic = buildEpic(*inputEpicSources.toTypedArray())
        )
        assert(isInvoked)
    }
}