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

package com.nlab.statekit.dsl.reduce

/**
 * @author Doohyun
 */
internal class DslEffectBuilder(
    private val scope: Any
) {
    private val effects = mutableListOf<DslEffect>()

    fun build(): DslEffect? = when (effects.size) {
        0 -> null
        1 -> effects.first()
        else -> DslEffect.Composite(scope, effects)
    }

    fun addEffect(effect: DslEffect) {
        effects.add(effect)
    }

    fun <R : Any, A : Any, S : Any> addNode(block: suspend (DslEffectScope<R, A, S>) -> Unit) {
        effects.add(
            DslEffect.Node(
                scope = scope,
                invoke = block
            )
        )
    }

    fun <A : Any, S : Any> addPredicateScope(
        isMatch: (UpdateSource<A, S>) -> Boolean,
        effect: DslEffect
    ) {
        effects.add(
            DslEffect.PredicateScope(
                scope = scope,
                isMatch = isMatch,
                effect = effect
            )
        )
    }

    fun <A : Any, S : Any, T : Any, U : Any> addTransformSourceScope(
        subScope: Any,
        transformSource: (UpdateSource<A, S>) -> UpdateSource<T, U>?,
        effect: DslEffect
    ) {
        effects.add(
            DslEffect.TransformSourceScope(
                scope = scope,
                subScope = subScope,
                transformSource = transformSource,
                effect = effect
            )
        )
    }
}