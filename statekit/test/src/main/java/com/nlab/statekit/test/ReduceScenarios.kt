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

package com.nlab.statekit.test

import com.nlab.statekit.ReducerScenario
import com.nlab.statekit.reduce.Reduce

/**
 * @author Thalys
 */
class ReduceScenarioInputSetup<A : Any, S : Any> internal constructor(
    private val reduce: Reduce<A, S>
) {
    fun input(action: A, current: S): ReduceScenario<A, S> = ReduceScenario(reduce, action, current)
}

class ReduceScenario<A : Any, S : Any> internal constructor(
    private val reduce: Reduce<A, S>,
    private val action: A,
    private val current: S
) {
    fun action(action: A) {

    }
}

class ReduceScenarioExpectedStateFromInitSetup<A : Any, S : Any> internal constructor(
    private val reduce: Reduce<A, S>,
    private val initState: S,
    private val action: A
) {
    fun expectedStateFromInit(transformInitToExpectedState: (S) -> S): ReducerScenario<A, S> =
        ReducerScenario(
            reduce,
            initState,
            action,
            transformInitToExpectedState
        )

    inline fun <reified T : S> expectedStateFromInitTypeOf(
        crossinline transformInitToExpectedState: (T) -> S
    ): ReducerScenario<A, S> = expectedStateFromInit { transformInitToExpectedState(it as T) }
}

fun