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

import com.nlab.statekit.reduce.Reduce

/**
 * @author Doohyun
 */
typealias RootScopeReduceBuilder<A, S> = ScopeReduceBuilder<A, S, A, S>

private const val DefaultScopeReduceScope = "RootScope"

@Suppress("FunctionName")
fun <A : Any, S : Any> DslReduce(defineDSL: RootScopeReduceBuilder<A, S>.() -> Unit): Reduce<A, S> =
    RootScopeReduceBuilder<A, S>(scope = DefaultScopeReduceScope)
        .apply(defineDSL)
        .let(::dslReduceOf)

private fun <A : Any, S : Any> dslReduceOf(reduceBuilder: RootScopeReduceBuilder<A, S>): Reduce<A, S> {
    val delegate = reduceBuilder.delegate
    return Reduce(
        transition = delegate.buildTransition()?.let(::transitionOf),
        effect = delegate.buildEffect()?.let(::effectOf)
    )
}