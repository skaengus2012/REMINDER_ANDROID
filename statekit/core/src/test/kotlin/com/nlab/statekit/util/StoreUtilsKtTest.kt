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

package com.nlab.statekit.util

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import com.nlab.statekit.middleware.epic.EpicClient
import com.nlab.statekit.middleware.epic.SubscriptionStrategy
import com.nlab.statekit.store.EpicClientFactory
import com.nlab.statekit.store.FetchCountableEpicClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

/**
 * @author thalys
 */
@ExperimentalCoroutinesApi
internal class StoreUtilsKtTest {
    private lateinit var testScope: CoroutineScope

    @Before
    fun setup() {
        testScope = CoroutineScope(Dispatchers.Unconfined)
    }

    @Test
    fun `Custom EpicClientFactory used, when epicClientFactory inputted`() = runTest {
        val epicClient = FetchCountableEpicClient()
        createStore<TestAction, TestState>(
            coroutineScope = testScope,
            baseState = MutableStateFlow(TestState.genState()),
            epic = buildDslEpic {
                whileStateUsed { flowOf(TestAction.genAction()) }
            },
            epicClientFactory = object : EpicClientFactory() {
                override fun onCreate(subscriptionStrategy: SubscriptionStrategy): EpicClient {
                    return epicClient
                }
            }
        )

        assertThat(epicClient.invokedCount, equalTo(1))
    }

    @Test
    fun `DefaultEpicClientFactory used, when epicClientFactory param was null`() = runTest {
        val actionFromEpic: TestAction = TestAction.genAction()
        val initState: TestState = TestState.State1
        val expectedState: TestState = TestState.State4()
        val store = createStore<TestAction, TestState>(
            coroutineScope = testScope,
            initState = initState,
            reducer = buildDslReducer {
                filteredAction(predicate = { it == actionFromEpic }) {
                    anyState { expectedState }
                }
            },
            epic = buildDslEpic {
                whileStateUsed { flowOf(actionFromEpic) }
            }
        )

        val actualState: TestState =
            store.state
                .filterIsInstance<TestState.State4>()
                .first()
        assertThat(actualState, sameInstance(expectedState))
    }

    @Test
    fun testWithoutConfig() = runTest {
        val initState = TestState.genState()
        val storeWithInitValue = createStore<TestAction, TestState>(
            testScope,
            initState
        )
        val storeWithBaseState = createStore<TestAction, TestState>(
            testScope,
            MutableStateFlow(initState)
        )

        listOf(
            storeWithInitValue.dispatch(TestAction.genAction()),
            storeWithBaseState.dispatch(TestAction.genAction())
        ).joinAll()

        assertThat(
            storeWithInitValue.state.value, sameInstance(initState)
        )
        assertThat(
            storeWithBaseState.state.value, sameInstance(initState)
        )
    }
}