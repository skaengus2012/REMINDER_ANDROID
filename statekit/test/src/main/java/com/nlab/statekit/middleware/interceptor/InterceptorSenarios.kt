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

package com.nlab.statekit.middleware.interceptor

import com.nlab.statekit.*
import com.nlab.statekit.testStoreCoroutineScope
import com.nlab.statekit.util.*
import kotlinx.coroutines.test.*

/**
 * @author Doohyun
 */
class InterceptScenarioInitStateSetup<A : Action, S : State> internal constructor(
    private val interceptor: Interceptor<A, S>
) {
    fun initState(state: S): InterceptScenarioActionSetup<A, S> = InterceptScenarioActionSetup(interceptor, state)
}

fun <A : Action, S : State> Interceptor<A, S>.scenario(): InterceptScenarioInitStateSetup<A, S> =
    InterceptScenarioInitStateSetup(interceptor = this)

class InterceptScenarioActionSetup<A : Action, S : State> internal constructor(
    private val interceptor: Interceptor<A, S>,
    private val initState: S
) {
    fun action(action: A): InterceptScenario<A, S> = InterceptScenario(
        interceptor,
        initState,
        action,
        emptyList()
    )
}

class InterceptScenario<A : Action, S : State> internal constructor(
    private val interceptor: Interceptor<A, S>,
    private val initState: S,
    private val action: A,
    private val additionalInterceptors: List<Interceptor<A, S>>
) {
    fun hook(block: suspend (A) -> Unit): InterceptScenario<A, S> =
        InterceptScenario(
            interceptor,
            initState,
            action,
            additionalInterceptors = additionalInterceptors + buildInterceptor { block(it.action) }
        )

    inline fun <reified T : A> hookIf(crossinline block: suspend (T) -> Unit): InterceptScenario<A, S> =
        hook { action ->
            if (action !is T) return@hook
            block(action)
        }

    suspend fun dispatchIn(testScope: TestScope) {
        var concatInterceptors: Interceptor<A, S> = interceptor
        additionalInterceptors.forEach { concatInterceptors += it }
        val store = createStore(
            testScope.testStoreCoroutineScope(),
            initState = initState,
            interceptor = concatInterceptors
        )
        store.dispatch(action).join()
    }
}