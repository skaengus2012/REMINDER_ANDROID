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

    class Node<out R : Any, out A : Any, out S : Any>(
        override val scope: Any,
        val invoke: suspend (DslSuspendEffectScope<@UnsafeVariance R, @UnsafeVariance A, @UnsafeVariance S>) -> Unit
    ) : DslEffect

    class Composite(
        override val scope: Any,
        val effects: List<DslEffect>
    ) : DslEffect {
        init {
            check(effects.size >= 2)
        }
    }

    class PredicateScope<out A : Any, out S : Any>(
        override val scope: Any,
        val isMatch: (UpdateSource<@UnsafeVariance A, @UnsafeVariance S>) -> Boolean,
        val effect: DslEffect
    ) : DslEffect

    class TransformSourceScope<out A : Any, out S : Any, out T : Any, out U : Any>(
        override val scope: Any,
        val subScope: Any,
        val transformSource: (UpdateSource<@UnsafeVariance A, @UnsafeVariance S>) -> UpdateSource<@UnsafeVariance T, @UnsafeVariance U>?,
        val effect: DslEffect
    ) : DslEffect
}

internal fun <A : Any, S : Any> effectOf(
    dslEffect: DslEffect
): Effect<A, S> = Effect.LifecycleNode { action, current, actionDispatcher, accPool, coroutineScope ->
    accPool.use { accEffect: Accumulator<DslEffect> ->
        accPool.use { accScope: Accumulator<Any> ->
            accPool.use { accDslEffectScope: Accumulator<DslSuspendEffectScope<A, Any, Any>> ->
                launch(
                    node = dslEffect,
                    scope = dslEffect.scope,
                    dslEffectScope = DslSuspendEffectScope(
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

private tailrec fun <A : Any> launch(
    node: DslEffect?,
    scope: Any,
    dslEffectScope: DslSuspendEffectScope<A, Any, Any>,
    accEffect: Accumulator<DslEffect>,
    accScope: Accumulator<Any>,
    accDslEffectScope: Accumulator<DslSuspendEffectScope<A, Any, Any>>,
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

    val nextNode: DslEffect?
    val nextScope: Any
    val nextDslEffectScope: DslSuspendEffectScope<A, Any, Any>
    when (node) {
        is DslEffect.Node<*, *, *> -> {
            coroutineScope.launch {
                @Suppress("UNCHECKED_CAST")
                (node as DslEffect.Node<A, Any, Any>).invoke(dslEffectScope)
            }
            nextNode = accEffect.removeLastOrNull()
            nextScope = scope
            nextDslEffectScope = dslEffectScope
        }
        is DslEffect.Composite -> {
            val childEffects = node.effects
            accEffect.addAllReversedWithoutHead(childEffects)
            nextNode = childEffects.first()
            nextScope = scope
            nextDslEffectScope = dslEffectScope
        }
        is DslEffect.PredicateScope<Any, Any> -> {
            nextNode = if (node.isMatch(dslEffectScope)) node.effect else accEffect.removeLastOrNull()
            nextScope = scope
            nextDslEffectScope = dslEffectScope
        }
        is DslEffect.TransformSourceScope<Any, Any, Any, Any> -> {
            val newSource = node.transformSource(dslEffectScope)
            if (newSource == null) {
                nextNode = accEffect.removeLastOrNull()
                nextScope = scope
                nextDslEffectScope = dslEffectScope
            } else {
                accScope.add(scope)
                accDslEffectScope.add(dslEffectScope)
                nextNode = node.effect
                nextScope = node.subScope
                nextDslEffectScope = DslSuspendEffectScope(newSource, dslEffectScope.actionDispatcher)
            }
        }
    }

    launch(
        node = nextNode,
        scope = nextScope,
        dslEffectScope = nextDslEffectScope,
        accEffect,
        accScope.apply { add(scope) },
        accDslEffectScope.apply { add(dslEffectScope) },
        coroutineScope
    )
}