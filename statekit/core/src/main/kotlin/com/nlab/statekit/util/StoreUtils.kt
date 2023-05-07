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

import com.nlab.statekit.Action
import com.nlab.statekit.Reducer
import com.nlab.statekit.State
import com.nlab.statekit.Store
import com.nlab.statekit.middleware.epic.Epic
import com.nlab.statekit.middleware.interceptor.Interceptor
import com.nlab.statekit.store.DefaultStoreFactory
import com.nlab.statekit.store.EpicClientFactory
import com.nlab.statekit.store.impl.DefaultEpicClientFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @author Doohyun
 */
private val storeFactory = DefaultStoreFactory()

fun <A : Action, S : State> createStore(
    coroutineScope: CoroutineScope,
    initState: S,
    reducer: Reducer<A, S> = buildDslReducer {},
    interceptor: Interceptor<A, S> = buildDslInterceptor {},
    epic: Epic<A> = buildEpic(),
    epicClientFactory: EpicClientFactory? = null
): Store<A, S> = createStore(
    coroutineScope,
    MutableStateFlow(initState),
    reducer,
    interceptor,
    epic,
    epicClientFactory
)

fun <A : Action, S : State> createStore(
    coroutineScope: CoroutineScope,
    baseState: MutableStateFlow<S>,
    reducer: Reducer<A, S> = buildDslReducer {},
    interceptor: Interceptor<A, S> = buildDslInterceptor {},
    epic: Epic<A> = buildEpic(),
    epicClientFactory: EpicClientFactory? = null
): Store<A, S> = storeFactory.createStore(
    coroutineScope.toStoreMaterialScope(),
    baseState,
    reducer,
    interceptor,
    epic,
    epicClientFactory = epicClientFactory ?: DefaultEpicClientFactory(baseState)
)