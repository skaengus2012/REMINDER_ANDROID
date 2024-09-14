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

package com.nlab.statekit.reduce

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


/**
 * @author Doohyun
 */
sealed interface Effect<A : Any, S : Any> {
    interface NodeEffect<A : Any, S : Any> : Effect<A, S> {
        fun needInvoke(action: A, current: S): Boolean
        suspend fun invoke(action: A, current: S, actionDispatcher: ActionDispatcher<A>)
    }

    class CompositeEffect<A : Any, S : Any>(
        val head: Effect<A, S>,
        vararg val tails: Effect<A, S>
    ) : Effect<A, S>
}

fun <A : Any, S : Any> Effect<A, S>.launch(
    action: A,
    current: S,
    actionDispatcher: ActionDispatcher<A>,
    coroutineScope: CoroutineScope,
) {
    tailrec fun <A : Any, S : Any> launchInternal(
        action: A,
        current: S,
        actionDispatcher: ActionDispatcher<A>,
        coroutineScope: CoroutineScope,
        node: Effect<A, S>?,
        acc: MutableList<Effect<A, S>>,
    ): S? = if (node == null) null else when (node) {
        is Effect.NodeEffect -> {
            if (node.needInvoke(action, current)) {
                coroutineScope.launch { node.invoke(action, current, actionDispatcher) }
            }
            launchInternal(action, current, actionDispatcher, coroutineScope, acc.removeLastOrNull(), acc)
        }

        is Effect.CompositeEffect -> {
            launchInternal(
                action,
                current,
                actionDispatcher,
                coroutineScope,
                node.head,
                acc.apply { addAll(node.tails) }
            )
        }
    }
    launchInternal(action, current, actionDispatcher, coroutineScope, node = this, acc = mutableListOf())
}