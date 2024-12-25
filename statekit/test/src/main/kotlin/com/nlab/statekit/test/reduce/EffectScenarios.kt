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
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext

/**
 * @author Doohyun
 */
class EffectScenarioInitSetup<A : Any, S : Any> internal constructor(
    private val reduce: Reduce<A, S>
) {
    fun <T : S> initState(state: T) = EffectScenarioActionSetup(reduce, state)
}

class EffectScenarioActionSetup<A : Any, S : Any, IS : S> internal constructor(
    private val reduce: Reduce<A, S>,
    private val initState: IS
) {
    fun <T : A> action(action: T) = EffectScenario(reduce, ScenarioInput(action, initState), emptyList())
}

class EffectScenario<A : Any, S : Any, IA : A, IS : S> internal constructor(
    private val reduce: Reduce<A, S>,
    private val input: ScenarioInput<IA, IS>,
    private val additionalEffects: List<Effect<A, S>>
) {
    fun hook(block: suspend TestEffectScope<A, S, IA, IS>.() -> Unit) = EffectScenario(
        reduce,
        input,
        additionalEffects = additionalEffects + Effect.SuspendNode { action, current, _ ->
            TestEffectScope(input, action, current).block()
        }
    )

    fun launchIn(
        coroutineScope: CoroutineScope,
        shouldLaunchWithTransition: Boolean = false
    ): EffectScenarioTestJob<IA, IS> {
        val reduce = if (shouldLaunchWithTransition) reduce else Reduce(effect = reduce.effect)
        val store = createStore(
            coroutineScope = coroutineScope,
            initState = input.initState,
            reduce = Reduce(effect = reduce.effect?.let { baseEffect ->
                additionalEffects.fold(baseEffect) { acc, effect -> Effect.Composite(effect, acc) }
            })
        )
        return EffectScenarioTestJob(
            input = input,
            job = store.dispatch(input.action)
        )
    }
}

class TestEffectScope<A : Any, S : Any, IA : A, IS : S> internal constructor(
    private val input: ScenarioInput<IA, IS>,
    val action: A,
    val current: S
) {
    val inputIAction: IA get() = input.action
    val inputState: IS get() = input.initState
}

class EffectScenarioTestJob<A : Any, S : Any> internal constructor(
    val input: ScenarioInput<A, S>,
    val job: Job
) {
    suspend fun join() {
        job.join()
    }

    suspend fun cancelAndJoin() {
        job.cancelAndJoin()
    }
}

suspend inline fun <A : Any, S : Any, IA : A, IS : S> EffectScenario<A, S, IA, IS>.launchAndJoin(
    shouldLaunchWithTransition: Boolean = false,
    verifyBlock: ScenarioInput<IA, IS>.() -> Unit = {}
) {
    val job = launchIn(CoroutineScope(currentCoroutineContext()), shouldLaunchWithTransition)
    job.join()
    verifyBlock(job.input)
}