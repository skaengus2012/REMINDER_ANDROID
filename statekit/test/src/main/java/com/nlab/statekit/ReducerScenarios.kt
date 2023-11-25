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

package com.nlab.statekit

import com.nlab.statekit.util.createStore
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

/**
 * @author Doohyun
 */
class ReducerScenarioInitStateSetup<A : Action, S : State> internal constructor(
    private val reducer: Reducer<A, S>
) {
    fun initState(state: S): ReducerScenarioActionSetup<A, S> = ReducerScenarioActionSetup(reducer, state)
}

fun <A : Action, S : State> Reducer<A, S>.scenario(): ReducerScenarioInitStateSetup<A, S> =
    ReducerScenarioInitStateSetup(reducer = this)

class ReducerScenarioActionSetup<A : Action, S : State> internal constructor(
    private val reducer: Reducer<A, S>,
    private val initState: S
) {
    fun action(action: A): ReducerScenarioExpectedStateFromInitSetup<A, S> =
        ReducerScenarioExpectedStateFromInitSetup(
            reducer,
            initState,
            action,
        )
}

class ReducerScenarioExpectedStateFromInitSetup<A : Action, S : State> internal constructor(
    private val reducer: Reducer<A, S>,
    private val initState: S,
    private val action: A
) {
    fun expectedStateFromInit(transformInitToExpectedState: (S) -> S): ReducerScenario<A, S> =
        ReducerScenario(
            reducer,
            initState,
            action,
            transformInitToExpectedState
        )

    inline fun <reified T : S> expectedStateFromInitTypeOf(
        crossinline transformInitToExpectedState: (T) -> S
    ): ReducerScenario<A, S> = expectedStateFromInit { transformInitToExpectedState(it as T) }
}

fun <A : Action, S : State> ReducerScenarioExpectedStateFromInitSetup<A, S>.expectedState(state: S) =
    expectedStateFromInit { state }

fun <A : Action, S : State> ReducerScenarioExpectedStateFromInitSetup<A, S>.expectedStateToInit() =
    expectedStateFromInit { it }

class ReducerScenario<A : Action, S : State> internal constructor(
    private val reducer: Reducer<A, S>,
    private val initState: S,
    private val action: A,
    private val transformInitToExpectedState: (S) -> S,
) {
    fun verify() = runTest {
        val expectedState = transformInitToExpectedState(initState)
        val store = createStore(testStoreCoroutineScope(), initState, reducer)
        store.dispatch(action).join()
        assertThat(store.state.value, equalTo(expectedState))
    }
}