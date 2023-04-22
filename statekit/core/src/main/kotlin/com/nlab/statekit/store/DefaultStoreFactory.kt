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
import com.nlab.statekit.middleware.enhancer.Enhancer
import com.nlab.statekit.middleware.epic.Epic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @author thalys
 */
internal class DefaultStoreFactory(
    private val epicSourceLoader: EpicSourceLoader
) {
    fun <A : Action, S : State> createStore(
        coroutineScope: CoroutineScope,
        initState: S,
        reducer: Reducer<A, S>,
        enhancer: Enhancer<A, S>,
        epic: Epic<A>,
    ): Store<A, S> {
        val stateFlow = MutableStateFlow(initState)
        val actionDispatcher = StoreActionDispatcher(stateFlow, reducer, enhancer)
        epicSourceLoader.load(coroutineScope, epicSources = epic(), actionDispatcher, stateFlow)

        return DefaultStore(coroutineScope, actionDispatcher, stateFlow)
    }
}