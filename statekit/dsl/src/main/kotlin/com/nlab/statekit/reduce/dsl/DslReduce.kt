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

package com.nlab.statekit.reduce.dsl

import com.nlab.statekit.reduce.ActionDispatcher
import com.nlab.statekit.reduce.Reduce

/**
 * @author Doohyun
 */
internal class DslReduce<A : Any, S : Any>(
    private val transition: (DslTransitionScope<A, S>) -> S,
    private val effect: suspend (DslEffectScope<A, S, A>) -> Unit
) : Reduce<A, S> {
    override fun transitionTo(action: A, current: S): S {
        return transition(DslTransitionScope(UpdateSource(action, current)))
    }

    override suspend fun launchEffect(action: A, current: S, actionDispatcher: ActionDispatcher<A>) {
        effect(DslEffectScope(UpdateSource(action, current), actionDispatcher))
    }
}