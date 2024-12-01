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

import com.nlab.statekit.reduce.Effect
import com.nlab.statekit.reduce.Reduce
import com.nlab.statekit.store.createStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext

/**
 * @author Doohyun
 */
class EffectScenarioActionSetup<A : Any, S : Any> internal constructor(
    reduce: Reduce<A, S>,
    shouldTestWithTransition: Boolean
) {
    private val reduce: Reduce<A, S> =
        if (shouldTestWithTransition) reduce
        else Reduce(effect = reduce.effect)

    fun action(action: A) = EffectScenarioCurrentSetup(reduce, action)
}

class EffectScenarioCurrentSetup<A : Any, S : Any> internal constructor(
    private val reduce: Reduce<A, S>,
    private val action: A
) {
    fun current(current: S) = EffectScenario(reduce, action, current, emptyList())
}

class EffectScenario<A : Any, S : Any> internal constructor(
    private val reduce: Reduce<A, S>,
    private val action: A,
    private val initState: S,
    private val additionalEffects: List<Effect<A, S>>
) {
    fun hook(block: suspend (A) -> Unit) = EffectScenario(
        reduce,
        action,
        initState,
        additionalEffects = additionalEffects + Effect.SuspendNode { action, _, _ -> block(action) }
    )

    inline fun <reified T : A> hookIf(crossinline block: suspend (T) -> Unit) = hook { action ->
        if (action !is T) return@hook
        block(action)
    }

    fun launchIn(coroutineScope: CoroutineScope): Job {
        val store = createStore(
            coroutineScope = coroutineScope,
            initState = initState,
            reduce = Reduce(effect = reduce.effect?.let { baseEffect ->
                additionalEffects.fold(baseEffect) { acc, effect -> Effect.Composite(effect, acc) }
            })
        )
        return store.dispatch(action)
    }

    suspend fun launchAndJoin() {
        launchIn(CoroutineScope(currentCoroutineContext())).join()
    }
}