/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.statekit.util

import com.nlab.statekit.middleware.interceptor.dsl.*
import com.nlab.statekit.middleware.interceptor.*
import com.nlab.statekit.*

/**
 * @author thalys
 */
fun <A : Action, S : State> buildInterceptor(
    block: suspend ActionDispatcher<A>.(UpdateSource<A, S>) -> Unit
): Interceptor<A, S> = DefaultInterceptor(block)

fun <A : Action, S : State> buildDslInterceptor(
    defineDSL: DslInterceptBuilder<A, S>.() -> Unit
): Interceptor<A, S> = DslInterceptor(defineDSL)

operator fun <A : Action, S : State> Interceptor<A, S>.plus(interceptor: Interceptor<A, S>): Interceptor<A, S> =
    buildInterceptor(
        block = CompositeInterceptBuilder<A, A, S>()
            .apply { add(this@plus) }
            .apply { add(interceptor) }
            .build()
    )

@Suppress("FunctionName")
fun <A : Action, S : State> EmptyInterceptor(): Interceptor<A, S> {
    return buildDslInterceptor {}
}