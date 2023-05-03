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

package com.nlab.statekit.middleware.interceptor.dsl

import com.nlab.statekit.Action
import com.nlab.statekit.State
import com.nlab.statekit.UpdateSource
import com.nlab.statekit.middleware.interceptor.Interceptor
import com.nlab.statekit.middleware.interceptor.ActionDispatcher

/**
 * @author thalys
 */
internal class DslInterceptor<A : Action, S : State>(
    defineDSL: DslInterceptBuilder<A, S>.() -> Unit
) : Interceptor<A, S> {
    private val block: suspend (ActionDispatcher<A>).(UpdateSource<A, S>) -> Unit =
        DslInterceptBuilder<A, S>()
            .apply(defineDSL)
            .build()
    override suspend fun invoke(actionDispatcher: ActionDispatcher<A>, updateSource: UpdateSource<A, S>) {
        block(actionDispatcher, updateSource)
    }
}