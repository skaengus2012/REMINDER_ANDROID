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

package com.nlab.reminder.core.state2.middleware.enhancer.dsl

import com.nlab.reminder.core.state2.Action
import com.nlab.reminder.core.state2.State
import com.nlab.reminder.core.state2.UpdateSource
import com.nlab.reminder.core.state2.middleware.enhancer.Enhancer
import com.nlab.reminder.core.state2.middleware.enhancer.SuspendActionDispatcher

/**
 * @author thalys
 */
internal class DslEnhancer<A : Action, S : State>(
    defineDSL: DslEnhanceBuilder<A, S>.() -> Unit
) : Enhancer<A, S> {
    private val block: suspend (SuspendActionDispatcher<A>).(UpdateSource<A, S>) -> Unit =
        DslEnhanceBuilder<A, S>()
            .apply(defineDSL)
            .build()
    override suspend fun invoke(actionDispatcher: SuspendActionDispatcher<A>, updateSource: UpdateSource<A, S>) {
        block(actionDispatcher, updateSource)
    }
}