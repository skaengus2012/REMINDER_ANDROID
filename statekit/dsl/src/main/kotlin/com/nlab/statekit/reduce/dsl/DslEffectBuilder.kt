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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
internal class DslEffectBuilder<A : Any, S : Any, R : Any> {
    private val effects = mutableListOf<suspend (DslEffectScope<A, S, R>) -> Unit>()

    fun add(block: suspend (DslEffectScope<A, S, R>) -> Unit) {
        effects += block
    }

    fun build(): suspend (DslEffectScope<A, S, R>) -> Unit = { scope ->
        coroutineScope(operateAsync(scope))
    }

    private fun operateAsync(
        scope: DslEffectScope<A, S, R>
    ): (CoroutineScope) -> Unit = { coroutineScope ->
        effects.forEach { coroutineScope.launch { it(scope) } }
    }
}