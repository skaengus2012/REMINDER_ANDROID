/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.statekit.store

import com.nlab.statekit.reduce.Reduce
import com.nlab.statekit.store.ComponentStore
import com.nlab.statekit.store.createComponentStore
import kotlinx.coroutines.flow.StateFlow

/**
 * @author Thalys
 */
interface ComponentStoreDelegate<A : Any, S : Any> {
    val state: StateFlow<S>
    suspend fun dispatch(action: A)
}

private class ComponentStoreDelegateImpl<A : Any, S : Any>(
    private val store: ComponentStore<A, S>
) : ComponentStoreDelegate<A, S> {
    override val state: StateFlow<S> = store.state
    override suspend fun dispatch(action: A) = store.dispatch(action)
}

fun <A : Any, S : Any> ComponentStoreDelegate(
    store: ComponentStore<A, S>
): ComponentStoreDelegate<A, S> = ComponentStoreDelegateImpl(store)

fun <A : Any, S : Any> ComponentStoreDelegate(
    initState: S,
    reduce: Reduce<A, S>,
): ComponentStoreDelegate<A, S> = ComponentStoreDelegate(createComponentStore(initState, reduce))