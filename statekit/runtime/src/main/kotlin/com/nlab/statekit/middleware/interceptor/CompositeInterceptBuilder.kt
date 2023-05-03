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

package com.nlab.statekit.middleware.interceptor

import com.nlab.statekit.Action
import com.nlab.statekit.State
import com.nlab.statekit.UpdateSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * @author thalys
 */

internal class CompositeInterceptBuilder<A : Action, T : A, S : State> {
    private val interceptors = mutableListOf<suspend ActionDispatcher<A>.(UpdateSource<T, S>) -> Unit>()

    fun add(block: suspend ActionDispatcher<A>.(UpdateSource<T, S>) -> Unit) {
        interceptors += block
    }

    fun build(): suspend ActionDispatcher<A>.(UpdateSource<T, S>) -> Unit = { updateSource ->
        coroutineScope(interceptAsync(actionDispatcher = this, updateSource))
    }

    private fun interceptAsync(
        actionDispatcher: ActionDispatcher<A>,
        updateSource: UpdateSource<T, S>
    ): (CoroutineScope) -> Unit = { scope ->
        interceptors.forEach { scope.launch { it(actionDispatcher, updateSource) } }
    }
}