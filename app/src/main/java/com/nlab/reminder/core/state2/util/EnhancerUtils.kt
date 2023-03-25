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

package com.nlab.reminder.core.state2.util

import com.nlab.reminder.core.state2.*
import com.nlab.reminder.core.state2.middleware.enhancer.*
import com.nlab.reminder.core.state2.middleware.enhancer.dsl.DslEnhanceBuilder
import com.nlab.reminder.core.state2.middleware.enhancer.dsl.DslEnhancer

/**
 * @author thalys
 */
fun <A : Action, S : State> buildEnhancer(
    block: suspend ActionDispatcher<A>.(UpdateSource<A, S>) -> Unit
): Enhancer<A, S> = DefaultEnhancer(block)

fun <A : Action, S : State> buildDslEnhancer(
    defineDSL: DslEnhanceBuilder<A, S>.() -> Unit
): Enhancer<A, S> = DslEnhancer(defineDSL)

operator fun <A : Action, S : State> Enhancer<A, S>.plus(enhancer: Enhancer<A, S>): Enhancer<A, S> =
    buildEnhancer(
        block = CompositeEnhanceBuilder<A, A, S>()
            .apply { add(this@plus) }
            .apply { add(enhancer) }
            .build()
    )