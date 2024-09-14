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

package com.nlab.statekit.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import com.nlab.statekit.*
import com.nlab.statekit.middleware.epic.Epic
import com.nlab.statekit.middleware.interceptor.Interceptor
import com.nlab.statekit.store.EpicClientFactory
import com.nlab.statekit.store.Store
import com.nlab.statekit.util.createStore as createStoreOrigin

/**
 * @author Doohyun
 */
fun <A : Action, S : State> ViewModel.createStore(
    initState: S,
    reducer: Reducer<A, S> = EmptyReducer(),
    interceptor: Interceptor<A, S> = EmptyInterceptor(),
    epic: Epic<A> = EmptyEpic(),
    epicClientFactory: EpicClientFactory? = null,
): Store<A, S> = createStoreOrigin(
    coroutineScope = viewModelScope,
    MutableStateFlow(initState),
    reducer,
    interceptor,
    epic,
    epicClientFactory
)

fun <A : Action, S : State> ViewModel.createStore(
    baseState: MutableStateFlow<S>,
    reducer: Reducer<A, S> = EmptyReducer(),
    interceptor: Interceptor<A, S> = EmptyInterceptor(),
    epic: Epic<A> = EmptyEpic(),
    epicClientFactory: EpicClientFactory? = null,
): Store<A, S> = createStoreOrigin(
    coroutineScope = viewModelScope,
    baseState,
    reducer,
    interceptor,
    epic,
    epicClientFactory
)