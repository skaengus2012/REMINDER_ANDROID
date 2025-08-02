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

import com.nlab.statekit.reduce.Effect
import com.nlab.statekit.reduce.Reduce
import com.nlab.statekit.reduce.composeReduce
import com.nlab.statekit.store.createStore
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * @author Doohyun
 */
class EffectScenario<A : Any, S : Any, IA : A, IS : S> internal constructor(
    private val reduce: Reduce<A, S>,
    private val current: IS,
    private val actionToDispatch: IA
) {
    context(testScope: TestScope)
    fun launchAndGetTrace(
        advance: Advance = Advance.UntilIdle
    ): List<LaunchedTrace<A, S, IA, IS>> = buildList {
        val scenarioInput = ScenarioInput(
            action = actionToDispatch,
            current = current
        )
        val store = createStore(
            coroutineScope = testScope + CoroutineExceptionHandler { _, throwable ->
                this += LaunchedTrace.Error(throwable)
            },
            initState = current,
            reduce = composeReduce(
                Reduce(
                    effect = Effect.Node { action, current ->
                        this += LaunchedTrace.ActionDispatched(
                            scenarioInput = scenarioInput,
                            action = action,
                            current = current
                        )
                    }
                ),
                reduce,
            )
        )
        store.dispatch(actionToDispatch)

        when (advance) {
            Advance.UntilIdle -> testScope.advanceUntilIdle()
            is Advance.TimeBy -> testScope.advanceTimeBy(delayTime = advance.delayTime)
        }
    }
}

sealed class LaunchedTrace<out A : Any, out S : Any, out IA : A, out IS : S> {
    data class Error(val throwable: Throwable) : LaunchedTrace<Nothing, Nothing, Nothing, Nothing>()
    data class ActionDispatched<A : Any, S : Any, IA : A, IS : S>(
        private val scenarioInput: ScenarioInput<IA, IS>,
        val action: A,
        val current: S
    ) : LaunchedTrace<A, S, IA, IS>()
}

sealed class Advance {
    object UntilIdle : Advance()
    class TimeBy(val delayTime: Duration) : Advance()

    companion object {
        fun TimeBy(delayTimeMillis: Long) = TimeBy(delayTime = delayTimeMillis.milliseconds)
    }
}
