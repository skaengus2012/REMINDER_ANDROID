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


/**
 * @author Doohyun
 */
internal class DslTransitionBuilder(
    private val scope: Any
) {
    private val transitions = mutableListOf<DslTransition>()

    fun build(): DslTransition? = when (transitions.size) {
        0 -> null
        1 -> transitions.first()
        else -> DslTransition.CompositeTransition(scope, transitions)
    }

    fun <R : Any, A : Any, S : R> addNodeTransition(block: (DslTransitionScope<A, S>) -> R) {
        transitions.add(
            DslTransition.NodeTransition(
                scope = scope,
                next = block
            )
        )
    }

    fun addScopeTransition(transition: DslTransition) {
        transitions.add(transition)
    }

    fun <A : Any, S : Any> addPredicateScopeTransition(
        isMatch: (UpdateSource<A, S>) -> Boolean,
        transition: DslTransition
    ) {
        transitions.add(
            DslTransition.PredicateScopeTransition(
                scope = scope,
                isMatch = isMatch,
                transition = transition,
            )
        )
    }

    fun <A : Any, S : Any, T : Any, U : Any> addTransformSourceScopeTransition(
        subScope: Any,
        transformSource: (UpdateSource<A, S>) -> UpdateSource<T, U>?,
        transition: DslTransition
    ) {
        transitions.add(
            DslTransition.TransformSourceScopeTransition(
                scope = scope,
                subScope = subScope,
                transformSource = transformSource,
                transition = transition,
            )
        )
    }
}