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

package com.nlab.statekit.store

import com.nlab.statekit.bootstrap.Bootstrap
import com.nlab.statekit.bootstrap.emptyBootstrap
import com.nlab.statekit.plugins.extension.toStoreMaterialScope
import com.nlab.statekit.reduce.Reduce
import com.nlab.statekit.reduce.emptyReduce
import kotlinx.coroutines.CoroutineScope

/**
 * @author Thalys
 */
private val storeFactory = DefaultStoreFactory()

fun <A : Any, S : Any> createStore(
    coroutineScope: CoroutineScope,
    initState: S,
    reduce: Reduce<A, S> = emptyReduce(),
    bootstrap: Bootstrap<A> = emptyBootstrap()
): Store<A, S> = storeFactory.createStore(
    coroutineScope.toStoreMaterialScope(),
    initState,
    reduce,
    bootstrap
)