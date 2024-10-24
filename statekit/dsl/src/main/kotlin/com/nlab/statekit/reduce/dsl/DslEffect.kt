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

import com.nlab.statekit.reduce.Accumulator
import com.nlab.statekit.reduce.Effect
import com.nlab.statekit.reduce.use
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * @author Thalys
 */
internal sealed interface DslEffect {
    val scope: Any

    class NodeEffect<out R : Any, out A : Any, out S : Any>(
        override val scope: Any,
        val invoke: suspend (DslEffectScope<@UnsafeVariance A, @UnsafeVariance S, out @UnsafeVariance R>) -> Unit
    ) : DslEffect

    class CompositeEffect(
        override val scope: Any,
        val effects: List<DslEffect>
    ) : DslEffect {
        init {
            check(effects.size >= 2)
        }
    }

    class PredicateScopeEffect<out A : Any, out S : Any>(
        override val scope: Any,
        val isMatch: (UnsafeUpdateSource<A, S>) -> Boolean,
        val effect: DslEffect
    ) : DslEffect

    class TransformSourceScopeEffect<out A : Any, out S : Any, out T : Any, out U : Any>(
        override val scope: Any,
        val subScope: Any,
        val transformSource: (UnsafeUpdateSource<A, S>) -> UnsafeUpdateSource<T, U>?,
        val effect: DslEffect
    ) : DslEffect
}

internal fun <A : Any, S : Any> Effect(
    dslEffect: DslEffect
): Effect<A, S> = Effect.LifecycleNodeEffect { action, current, actionDispatcher, coroutineScope, accumulatorPool ->
    accumulatorPool.use { accEffect: Accumulator<DslEffect> ->
        accumulatorPool.use { accScope: Accumulator<Any> ->
            accumulatorPool.use { accDslEffectScope: Accumulator<DslEffectScope<Any, Any, A>> ->
                launch(
                    node = dslEffect,
                    scope = dslEffect.scope,
                    dslEffectScope = DslEffectScope(
                        UpdateSource(action, current),
                        actionDispatcher
                    ),
                    accEffect = accEffect,
                    accScope = accScope,
                    accDslEffectScope = accDslEffectScope,
                    coroutineScope = coroutineScope
                )
            }
        }
    }
}

private fun <A : Any>launch(
    node: DslEffect?,
    scope: Any,
    dslEffectScope: DslEffectScope<Any, Any, @UnsafeVariance A>,
    accEffect: Accumulator<DslEffect>,
    accScope: Accumulator<Any>,
    accDslEffectScope: Accumulator<DslEffectScope<Any, Any, @UnsafeVariance A>>,
    coroutineScope: CoroutineScope
) {
    if (node == null) return

    if (scope !== node.scope) {
        launch(
            node,
            accScope.removeLast(),
            accDslEffectScope.removeLast(),
            accEffect,
            accScope,
            accDslEffectScope,
            coroutineScope
        )
        return
    }

    when (node) {
        is DslEffect.NodeEffect<Any, Any, Any> -> {
            coroutineScope.launch { node.invoke(dslEffectScope) }
            launch(
                node = accEffect.removeLastOrNull(),
                scope,
                dslEffectScope,
                accEffect,
                accScope,
                accDslEffectScope,
                coroutineScope
            )
        }
        is DslEffect.CompositeEffect -> {
            val childEffects = node.effects
            launch(
                node = childEffects.first(),
                scope,
                dslEffectScope,
                accEffect.apply {
                    for (index in 1 until childEffects.size) add(childEffects[index])
                },
                accScope,
                accDslEffectScope,
                coroutineScope
            )
        }
        is DslEffect.PredicateScopeEffect<Any, Any> -> {
            launch(
                node = if (node.isMatch(dslEffectScope)) node.effect else accEffect.removeLastOrNull(),
                scope,
                dslEffectScope,
                accEffect,
                accScope,
                accDslEffectScope,
                coroutineScope
            )
        }
        is DslEffect.TransformSourceScopeEffect<Any, Any, Any, Any> -> {
            val newSource = node.transformSource(dslEffectScope)
            if (newSource == null) {
                launch(
                    node = accEffect.removeLastOrNull(),
                    scope,
                    dslEffectScope,
                    accEffect,
                    accScope,
                    accDslEffectScope,
                    coroutineScope
                )
            } else {
                launch(
                    node = node.effect,
                    scope = node.subScope,
                    dslEffectScope = DslEffectScope(newSource, dslEffectScope.actionDispatcher),
                    accEffect,
                    accScope.apply { add(scope) },
                    accDslEffectScope.apply { add(dslEffectScope) },
                    coroutineScope
                )
            }
        }
    }
}