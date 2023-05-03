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

package com.nlab.statekit.store

import com.nlab.statekit.Action
import com.nlab.statekit.Reducer
import com.nlab.statekit.State
import com.nlab.statekit.UpdateSource
import com.nlab.statekit.middleware.enhancer.ActionDispatcher
import com.nlab.statekit.middleware.enhancer.Enhancer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate

/**
 * @author thalys
 */
internal class StoreActionDispatcher<A : Action, S : State>(
    private val state: MutableStateFlow<S>,
    private val reduce: Reducer<A, S>,
    private val enhance: Enhancer<A, S>
) : ActionDispatcher<A> {
    override suspend fun dispatch(action: A) {
        enhance(this, UpdateSource(action, before = state.getAndUpdate { cur -> reduce(UpdateSource(action, cur)) }))
    }
}