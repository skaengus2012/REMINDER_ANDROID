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

package com.nlab.statekit.test

import com.nlab.statekit.Action
import com.nlab.statekit.State
import com.nlab.statekit.middleware.epic.Epic
import com.nlab.statekit.util.buildDslInterceptor
import com.nlab.statekit.util.createStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

/**
 * @author Doohyun
 */
class EpicTester<A : Action> internal constructor(
    private val epic: Epic<A>,
    private val expectedAction: A?,
) {
    fun expectedAction(expectedAction: A): EpicTester<A> =
        EpicTester(epic, expectedAction)

    fun verify() = runTest {
        checkNotNull(expectedAction) { "ExpectedAction must not be null" }

        val awaitSummaryLoadedReceived = CompletableDeferred<Action>()
        val store = createStore(
            testStoreCoroutineScope(),
            EpicTestState(),
            interceptor = buildDslInterceptor {
                anyAction {
                    anyState { awaitSummaryLoadedReceived.complete(it.action) }
                }
            },
            epic = epic
        )
        val collectJob = launch { store.state.collect() }

        assertThat(awaitSummaryLoadedReceived.await(), equalTo(expectedAction))
        collectJob.cancelAndJoin()
    }
}

private class EpicTestState : State

fun <A : Action> Epic<A>.tester(): EpicTester<A> =
    EpicTester(epic = this, expectedAction = null)
