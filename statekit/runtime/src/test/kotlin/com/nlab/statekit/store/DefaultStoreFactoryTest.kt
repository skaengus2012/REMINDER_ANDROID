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

package com.nlab.statekit.store

import com.nlab.statekit.middleware.interceptor.*
import com.nlab.statekit.middleware.epic.*
import com.nlab.statekit.util.*
import com.nlab.statekit.*
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
internal class DefaultStoreFactoryTest {

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
    fun `Interceptor modified state of store`() = runTest {
        val initState: TestState = TestState.State1
        val expectedState: TestState = TestState.State2
        val input: TestAction = TestAction.Action1
        val actionInInterceptor: TestAction = TestAction.Action2
        val store = createStoreFromDefaultStoreFactory(
            coroutineScope = this,
            initState = initState,
            reducer = buildDslReducer {
                filteredAction(predicate = { it == actionInInterceptor }) {
                    anyState { expectedState }
                }
            },
            interceptor = buildDslInterceptor {
                filteredState(predicate = { it == initState }) {
                    filteredAction(predicate = { it == input }) { dispatch(actionInInterceptor) }
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
        val actionStream = emptyFlow<TestAction>()
        val subscriptionStrategy = genSubscriptionStrategy()
        val epicSourceSize: Int = genIntGreaterThanZero()
        val epicClient = object : EpicClient {
            var invokedCount: Int = 0
            override fun <A : Action> fetch(
                coroutineScope: CoroutineScope,
                epicStream: Flow<A>,
                actionDispatcher: ActionDispatcher<A>
            ): Job {
                ++invokedCount
                return Job()
            }
        }

        createStoreFromDefaultStoreFactory(
            coroutineScope = this,
            epic = buildEpic(*Array(epicSourceSize) { EpicSource(actionStream, subscriptionStrategy) }),
            epicClientFactory = object : EpicClientFactory() {
                override fun onCreate(subscriptionStrategy: SubscriptionStrategy): EpicClient {
                    return epicClient
                }
            }
        )
        assertThat(epicClient.invokedCount, equalTo(epicSourceSize))
    }

    private fun createStoreFromDefaultStoreFactory(
        coroutineScope: CoroutineScope,
        initState: TestState = TestState.genState(),
        reducer: Reducer<TestAction, TestState> = mock(),
        interceptor: Interceptor<TestAction, TestState> = mock(),
        epic: Epic<TestAction> = mock(),
        epicClientFactory: EpicClientFactory = mock()
    ): Store<TestAction, TestState> =
        DefaultStoreFactory().createStore(
            coroutineScope, MutableStateFlow(initState), reducer, interceptor, epic, epicClientFactory
        )
}