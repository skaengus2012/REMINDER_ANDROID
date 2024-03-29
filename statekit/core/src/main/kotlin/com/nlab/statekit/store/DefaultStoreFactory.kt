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
import com.nlab.statekit.Store
import com.nlab.statekit.middleware.interceptor.Interceptor
import com.nlab.statekit.middleware.epic.Epic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * @author thalys
 */
internal class DefaultStoreFactory {
    fun <A : Action, S : State> createStore(
        coroutineScope: CoroutineScope,
        baseState: MutableStateFlow<S>,
        reducer: Reducer<A, S>,
        interceptor: Interceptor<A, S>,
        epic: Epic<A>,
        epicClientFactory: EpicClientFactory
    ): Store<A, S> {
        val actionDispatcher = StoreActionDispatcher(baseState, reducer, interceptor)
        return DefaultStore(
            baseState.asStateFlow(),
            coroutineScope,
            actionDispatcher,
            initJobs = epic().map { epicSource ->
                epicClientFactory
                    .create(epicSource.subscriptionStrategy)
                    .fetch(coroutineScope, epicSource.stream, actionDispatcher)
            }
        )
    }
}