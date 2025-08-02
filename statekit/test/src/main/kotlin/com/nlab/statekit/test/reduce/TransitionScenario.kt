/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.statekit.test.reduce

import com.nlab.statekit.reduce.Reduce
import com.nlab.statekit.store.createStore
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

/**
 * @author Doohyun
 */
class TransitionScenario<A : Any, S : Any, IA : A, IS : S> internal constructor(
    private val reduce: Reduce<A, S>,
    private val current: IS,
    private val actionToDispatch: IA
) {
    context(testScope: TestScope)
    fun expectedNextState(block: ScenarioInput<IA, IS>.() -> S) {
        val store = createStore(
            coroutineScope = testScope,
            initState = current,
            reduce = reduce
        )
        val expectedNextState = block(ScenarioInput(action = actionToDispatch, current = current))
        store.dispatch(actionToDispatch)
        testScope.advanceUntilIdle()
        assertThat(
            store.state.value,
            equalTo(expectedNextState)
        )
    }

    context(testScope: TestScope)
    fun getTransitionResult(): S {
        val store = createStore(
            coroutineScope = testScope,
            initState = current,
            reduce = reduce
        )
        store.dispatch(actionToDispatch)
        testScope.advanceUntilIdle()
        return store.state.value
    }
}

context(testScope: TestScope)
fun <A : Any, S : Any> TransitionScenario<A, S, *, *>.expectedNextState(state: S) =
    expectedNextState { state }

context(testScope: TestScope)
fun <A : Any, S : Any, IS : S> TransitionScenario<A, S, *, IS>.expectedNotChanged() =
    expectedNextState { current }