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

import com.nlab.statekit.middleware.interceptor.ActionDispatcher
import com.nlab.statekit.Action
import com.nlab.statekit.State
import com.nlab.statekit.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * @author thalys
 */
internal class DefaultStore<A : Action, S : State>(
    override val state: StateFlow<S>,
    private val coroutineScope: CoroutineScope,
    private val actionDispatcher: ActionDispatcher<A>,
    @Suppress("unused") private val initJobs: List<Job>  // for strong reference
) : Store<A, S>() {
    override fun dispatch(action: A): Job = coroutineScope.launch { actionDispatcher.dispatch(action) }
}