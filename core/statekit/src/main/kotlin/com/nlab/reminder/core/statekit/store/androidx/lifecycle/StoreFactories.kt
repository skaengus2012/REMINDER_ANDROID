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

package com.nlab.reminder.core.statekit.store.androidx.lifecycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nlab.statekit.bootstrap.Bootstrap
import com.nlab.statekit.bootstrap.EmptyBootstrap
import com.nlab.statekit.reduce.EmptyReduce
import com.nlab.statekit.reduce.Reduce
import com.nlab.statekit.store.Store
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus
import com.nlab.statekit.store.createStore as createStoreOrigin

/**
 * @author Doohyun
 */
internal var globalExceptionHandlers: List<CoroutineExceptionHandler> = emptyList()

fun <A : Any, S : Any> ViewModel.createStore(
    initState: S,
    reduce: Reduce<A, S> = EmptyReduce(),
    bootstrap: Bootstrap<A> = EmptyBootstrap()
): Store<A, S> = createStoreOrigin(
    coroutineScope = createStoreMaterialScope(),
    initState = initState,
    reduce = reduce,
    bootstrap = bootstrap
)

fun ViewModel.createStoreMaterialScope(): CoroutineScope {
    var ret = viewModelScope
    ret += Dispatchers.Default
    ret.setupGlobalAndLocalMergedCoroutineExceptionHandler { ret += it }
    return ret
}

private fun CoroutineScope.setupGlobalAndLocalMergedCoroutineExceptionHandler(
    block: (CoroutineExceptionHandler) -> Unit
) {
    val globals = globalExceptionHandlers.takeIf { it.isEmpty() } ?: return
    val local = coroutineContext[CoroutineExceptionHandler]
    val exceptionHandler = when {
        local == null && globals.size == 1 -> globals.first()
        else -> CoroutineExceptionHandler { context, throwable ->
            local?.handleException(context, throwable)
            globals.forEach { it.handleException(context, throwable) }
        }
    }
    block(exceptionHandler)
}