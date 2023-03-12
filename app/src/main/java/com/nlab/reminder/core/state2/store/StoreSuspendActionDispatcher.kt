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

package com.nlab.reminder.core.state2.store

import com.nlab.reminder.core.state2.Action
import com.nlab.reminder.core.state2.Reducer
import com.nlab.reminder.core.state2.State
import com.nlab.reminder.core.state2.UpdateSource
import com.nlab.reminder.core.state2.middleware.handle.SuspendActionDispatcher
import com.nlab.reminder.core.state2.middleware.handle.UpdateSourceHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate

/**
 * @author thalys
 */
class StoreSuspendActionDispatcher<A : Action, S : State>(
    private val state: MutableStateFlow<S>,
    private val reduce: Reducer<A, S>,
    private val handleUpdateSource: UpdateSourceHandle<A, S>
) : SuspendActionDispatcher<A> {
    override suspend fun dispatch(action: A) {
        handleUpdateSource(
            actionDispatcher = this,
            UpdateSource(
                action,
                before = state.getAndUpdate { cur -> reduce(UpdateSource(action, cur)) }
            )
        )
    }
}