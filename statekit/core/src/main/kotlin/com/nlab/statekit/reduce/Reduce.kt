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

import com.nlab.statekit.internal.merge

/**
 * @author Doohyun
 */
class Reduce<A : Any, S : Any>(
    val transition: Transition<A, S>? = null,
    val effect: Effect<A, S>? = null
)

@Suppress("FunctionName")
fun <A : Any, S : Any> EmptyReduce(): Reduce<A, S> = Reduce()

fun <A : Any, S : Any> composeReduce(
    reduces: List<Reduce<A, S>>
): Reduce<A, S> = Reduce(
    transition = reduces.compositeTransitions,
    effect = reduces.compositeEffects
)

fun <A : Any, S : Any> composeReduce(
    first: Reduce<A, S>,
    second: Reduce<A, S>,
    vararg etc: Reduce<A, S>
): Reduce<A, S> = composeReduce(buildList {
    add(first)
    add(second)
    if (etc.isNotEmpty()) addAll(etc)
})

private val <A : Any, S : Any> List<Reduce<A, S>>.compositeTransitions: Transition<A, S>?
    get() = mapNotNull { it.transition }.merge { head, tails -> Transition.Composite(head, tails) }

private val <A : Any, S : Any> List<Reduce<A, S>>.compositeEffects: Effect<A, S>?
    get() = mapNotNull { it.effect }.merge { head, tails -> Effect.Composite(head, tails) }