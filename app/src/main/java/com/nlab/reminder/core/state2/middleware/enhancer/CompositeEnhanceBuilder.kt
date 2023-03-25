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

package com.nlab.reminder.core.state2.middleware.enhancer

import com.nlab.reminder.core.state2.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * @author thalys
 */

internal class CompositeEnhanceBuilder<A : Action, T : A, S : State> {
    private val enhances = mutableListOf<suspend ActionDispatcher<A>.(UpdateSource<T, S>) -> Unit>()

    fun add(block: suspend ActionDispatcher<A>.(UpdateSource<T, S>) -> Unit) {
        enhances += block
    }

    fun build(): suspend ActionDispatcher<A>.(UpdateSource<T, S>) -> Unit = { updateSource ->
        coroutineScope(enhanceAsync(actionDispatcher = this, updateSource))
    }

    private fun enhanceAsync(
        actionDispatcher: ActionDispatcher<A>,
        updateSource: UpdateSource<T, S>
    ): (CoroutineScope) -> Unit = { scope ->
        enhances.forEach { scope.launch { it(actionDispatcher, updateSource) } }
    }
}