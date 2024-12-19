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

package com.nlab.statekit.test.reduce

import com.nlab.statekit.reduce.Reduce
import com.nlab.statekit.store.createStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

/**
 * @author Doohyun
 */
class TransitionScenarioInitSetup<A : Any, S : Any> internal constructor(
    private val reduce: Reduce<A, S>,
) {
    fun <T : S> initState(state: T) = TransitionScenarioActionSetup(reduce, state)
}

class TransitionScenarioActionSetup<A : Any, S : Any, IS : S> internal constructor(
    private val reduce: Reduce<A, S>,
    private val initState: IS
) {
    fun <T : A> action(action: T) = TransitionScenarioExpectedStateSetup(reduce, initState, action)
}

class TransitionScenarioExpectedStateSetup<A : Any, S : Any, IA : A, IS : S> internal constructor(
    private val reduce: Reduce<A, S>,
    private val initState: IS,
    private val action: IA,
) {
    fun expectedStateFromInput(block: ScenarioInput<IA, IS>.() -> S) = TransitionScenario(
        reduce,
        ScenarioInput(action, initState),
        block
    )
}

class TransitionScenario<A : Any, S : Any, IA : A, IS : S> internal constructor(
    private val reduce: Reduce<A, S>,
    private val input: ScenarioInput<IA, IS>,
    private val transformInitToExpectedState: ScenarioInput<IA, IS>.() -> S,
) {
    suspend fun verify(shouldVerifyWithEffect: Boolean = false) {
        val store = createStore(
            coroutineScope = CoroutineScope(currentCoroutineContext()),
            initState = input.initState,
            reduce = if (shouldVerifyWithEffect) reduce else Reduce(transition = reduce.transition)
        )
        store.dispatch(input.action).join()
        assertThat(
            store.state.value,
            equalTo(transformInitToExpectedState(input))
        )
    }
}

fun <A : Any, S : Any> TransitionScenarioExpectedStateSetup<A, S, *, *>.expectedState(state: S) =
    expectedStateFromInput { state }

fun <A : Any, S : Any, IS : S> TransitionScenarioExpectedStateSetup<A, S, *, IS>.expectedStateToInit() =
    expectedStateFromInput { initState }