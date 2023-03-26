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

package com.nlab.reminder.core.state2.middleware.epic.infra

import com.nlab.reminder.core.state2.Action
import com.nlab.reminder.core.state2.middleware.enhancer.ActionDispatcher
import com.nlab.reminder.core.state2.middleware.epic.EpicClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * @author thalys
 */
internal class WhileStateUsedEpicClient(private val stateFlow: MutableStateFlow<*>) : EpicClient {
    override fun <A : Action> fetch(
        coroutineScope: CoroutineScope,
        epicStream: Flow<A>,
        actionDispatcher: ActionDispatcher<A>
    ) {
        coroutineScope.launch {
            stateFlow.subscriptionCount
                .map { it > 0 }
                .distinctUntilChanged()
                // no test.
                // jacoco cannot recognize collectLatest.
                .collectLatest { epicStream.collect(actionDispatcher::dispatch) }
        }
    }
}