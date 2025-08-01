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

import com.nlab.statekit.reduce.NodeStack
import com.nlab.statekit.reduce.Effect
import com.nlab.statekit.reduce.ThrowableCollector
import com.nlab.statekit.reduce.use
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * @author Thalys
 */
internal sealed interface DslEffect {
    val scope: Any

    class Node<out A : Any, out S : Any>(
        override val scope: Any,
        val invoke: (DslEffectScope<@UnsafeVariance A, @UnsafeVariance S>) -> Unit
    ) : DslEffect

    class SuspendNode<out R : Any, out A : Any, out S : Any>(
        override val scope: Any,
        val invoke: suspend (DslSuspendEffectScope<@UnsafeVariance R, @UnsafeVariance A, @UnsafeVariance S>) -> Unit
    ) : DslEffect

    class Composite(
        override val scope: Any,
        val head: DslEffect,
        val tails: List<DslEffect>
    ) : DslEffect

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
): Effect<A, S> = Effect.LifecycleNode { action, current, context, actionDispatcher ->
    val nodeStackPool = context.nodeStackPool
    nodeStackPool.use { accEffect: NodeStack<DslEffect> ->
        nodeStackPool.use { accScope: NodeStack<Any> ->
            nodeStackPool.use { accDslEffectScope: NodeStack<DslSuspendEffectScope<A, Any, Any>> ->
                launch(
                    node = dslEffect,
                    scope = dslEffect.scope,
                    dslEffectScope = DslSuspendEffectScope(UpdateSource(action, current), actionDispatcher),
                    accEffect = accEffect,
                    accScope = accScope,
                    accDslEffectScope = accDslEffectScope,
                    coroutineScope = context.coroutineScope,
                    throwableCollector = context.throwableCollector
                )
            }
        }
    }
}

private tailrec fun <A : Any> launch(
    node: DslEffect?,
    scope: Any,
    dslEffectScope: DslSuspendEffectScope<A, Any, Any>,
    accEffect: NodeStack<DslEffect>,
    accScope: NodeStack<Any>,
    accDslEffectScope: NodeStack<DslSuspendEffectScope<A, Any, Any>>,
    coroutineScope: CoroutineScope,
    throwableCollector: ThrowableCollector
) {
    if (node == null) return

    if (scope !== node.scope) {
        launch(
            node = node,
            scope = accScope.removeLast(),
            dslEffectScope = accDslEffectScope.removeLast(),
            accEffect = accEffect,
            accScope = accScope,
            accDslEffectScope = accDslEffectScope,
            coroutineScope = coroutineScope,
            throwableCollector = throwableCollector
        )
        return
    }

    val nextNode: DslEffect?
    val nextScope: Any
    val nextDslEffectScope: DslSuspendEffectScope<A, Any, Any>
    when (node) {
        is DslEffect.Node<*, *> -> {
            try {
                node.invoke(dslEffectScope)
            } catch (t: Throwable) {
                throwableCollector.collect(t)
            }

            nextNode = accEffect.removeLastOrNull()
            nextScope = scope
            nextDslEffectScope = dslEffectScope
        }

        is DslEffect.SuspendNode<*, *, *> -> {
            coroutineScope.launch {
                try {
                    @Suppress("UNCHECKED_CAST")
                    (node as DslEffect.SuspendNode<A, Any, Any>).invoke(dslEffectScope)
                } catch (t: Throwable) {
                    throwableCollector.collect(t)
                }
            }
            nextNode = accEffect.removeLastOrNull()
            nextScope = scope
            nextDslEffectScope = dslEffectScope
        }

        is DslEffect.Composite -> {
            accEffect.addAllReversed(node.tails)
            nextNode = node.head
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
        accEffect = accEffect,
        accScope = accScope.apply { add(scope) },
        accDslEffectScope = accDslEffectScope.apply { add(dslEffectScope) },
        coroutineScope = coroutineScope,
        throwableCollector = throwableCollector
    )
}