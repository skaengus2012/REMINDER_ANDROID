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

/**
 * @author Doohyun
 */
class TestScenarioSelectionBuilder<A : Any, S : Any, IA : A, IS : S> internal constructor(
    private val reduce: Reduce<A, S>,
    private val current: IS,
    private val actionToDispatch: IA
) {
    fun transitionScenario(dispatchWithEffect: Boolean = false) = TransitionScenario(
        reduce = if (dispatchWithEffect) reduce else Reduce(transition = reduce.transition),
        current = current,
        actionToDispatch = actionToDispatch
    )

    fun effectScenario(dispatchWithTransition: Boolean = false) = EffectScenario(
        reduce = if (dispatchWithTransition) reduce else Reduce(effect = reduce.effect),
        current = current,
        actionToDispatch = actionToDispatch
    )
}