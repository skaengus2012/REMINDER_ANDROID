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
import com.nlab.statekit.store.createComponentStore
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

/**
 * @author Doohyun
 */
class TransitionScenarioActionSetup<A : Any, S : Any> internal constructor(
    reduce: Reduce<A, S>,
    shouldTestWithEffect: Boolean
) {
    private val reduce: Reduce<A, S> =
        if (shouldTestWithEffect) reduce
        else Reduce(transition = reduce.transition)

    fun action(action: A) = TransitionScenarioCurrentSetup(reduce, action)
}

class TransitionScenarioCurrentSetup<A : Any, S : Any> internal constructor(
    private val reduce: Reduce<A, S>,
    private val action: A
) {
    fun current(current: S) = TransitionScenarioExpectedStateSetup(reduce, action, current)
}

class TransitionScenarioExpectedStateSetup<A : Any, S : Any> internal constructor(
    private val reduce: Reduce<A, S>,
    private val action: A,
    private val current: S,
) {
    fun expectedStateFromInit(transformInitToExpectedState: (S) -> S) = TransitionScenario(
        reduce,
        action,
        current,
        transformInitToExpectedState
    )

    inline fun <reified T : S> expectedStateFromInitTypeOf(
        crossinline transformInitToExpectedState: (T) -> S
    ) = expectedStateFromInit { transformInitToExpectedState(it as T) }
}

fun <A : Any, S : Any> TransitionScenarioExpectedStateSetup<A, S>.expectedState(state: S) =
    expectedStateFromInit { state }

fun <A : Any, S : Any> TransitionScenarioExpectedStateSetup<A, S>.expectedStateToInit() =
    expectedStateFromInit { it }

class TransitionScenario<A : Any, S : Any> internal constructor(
    private val reduce: Reduce<A, S>,
    private val action: A,
    private val current: S,
    private val transformInitToExpectedState: (S) -> S,
) {
    suspend fun verify() {
        val store = createComponentStore(
            initState = current,
            reduce = reduce
        )
        store.dispatch(action)
        assertThat(store.state.value, equalTo(transformInitToExpectedState(current)))
    }
}