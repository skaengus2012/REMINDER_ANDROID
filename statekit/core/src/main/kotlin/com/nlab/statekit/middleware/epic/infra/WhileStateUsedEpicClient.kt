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

package com.nlab.statekit.middleware.epic.infra

import com.nlab.statekit.Action
import com.nlab.statekit.middleware.interceptor.ActionDispatcher
import com.nlab.statekit.middleware.epic.EpicClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * @author thalys
 */
internal class WhileStateUsedEpicClient(private val stateFlow: MutableStateFlow<*>) : EpicClient {
    override fun <A : Action> fetch(
        coroutineScope: CoroutineScope,
        epicStream: Flow<A>,
        actionDispatcher: ActionDispatcher<A>
    ): Job {
        return coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            stateFlow.subscriptionCount
                .map { it > 0 }
                .distinctUntilChanged()
                // no test.
                // jacoco cannot recognize collectLatest.
                .collectLatest { isActive ->
                    if (isActive) {
                        println("구독 중.")
                        epicStream.collect(actionDispatcher::dispatch)
                    } else {
                        println("구독 종료.")
                    }
                }
        }
    }
}