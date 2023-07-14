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
import com.nlab.statekit.Reducer
import com.nlab.statekit.State
import com.nlab.statekit.util.createStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

/**
 * @author Doohyun
 */
class ReduceTester<A : Action, S : State> internal constructor(
    private val reducer: Reducer<A, S>,
    private val dispatchAction: A?,
    private val initState: S?,
    private val transformInitToExpectedState: ((S) -> S)?
) {
    fun dispatchAction(action: A) = ReduceTester(reducer, action, initState, transformInitToExpectedState)
    fun initState(state: S) = ReduceTester(reducer, dispatchAction, state, transformInitToExpectedState)

    fun expectedState(transformInitToExpectedState: (S) -> S): ReduceTester<A, S> =
        ReduceTester(reducer, dispatchAction, initState, transformInitToExpectedState)

    fun expectedState(state: S): ReduceTester<A, S> =
        ReduceTester(reducer, dispatchAction, initState, transformInitToExpectedState = { state })

    suspend fun verify(testScope: TestScope) {
        checkNotNull(dispatchAction) { "DispatchAction must not be null" }
        checkNotNull(initState) { "InitState must not be null" }

        val expectedState = checkNotNull(transformInitToExpectedState?.invoke(initState))
        val store = createStore(CoroutineScope(UnconfinedTestDispatcher(testScope.testScheduler)), initState, reducer)
        store.dispatch(dispatchAction).join()
        assertThat(store.state.value, equalTo(expectedState))
    }
}

fun <A : Action, S : State> Reducer<A, S>.tester(): ReduceTester<A, S> =
    ReduceTester(reducer = this, dispatchAction = null, initState = null, transformInitToExpectedState = null)
