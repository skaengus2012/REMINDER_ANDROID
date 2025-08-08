/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext

/**
 * @author Doohyun
 */
internal var globalExceptionHandlers: List<CoroutineExceptionHandler> = emptyList()

fun CoroutineScope.toStoreMaterialScope(): CoroutineScope {
    var ret = this
    ret += Dispatchers.Default.limitedParallelism(parallelism = 1)
    val coroutineExceptionHandler = combinedGlobalAndLocalCoroutineExceptionHandler()
    if (coroutineExceptionHandler != null) {
        ret += coroutineExceptionHandler
    }
    return ret
}

private fun CoroutineScope.combinedGlobalAndLocalCoroutineExceptionHandler(): CoroutineExceptionHandler? {
    val globalHandlers = globalExceptionHandlers.takeIf { it.isNotEmpty() } ?: return null
    val localHandler = coroutineContext[CoroutineExceptionHandler]
    return if (localHandler == null) {
        if (globalHandlers.size == 1) globalHandlers.first()
        else CoroutineExceptionHandler { context, throwable -> globalHandlers.handleException(context, throwable) }
    } else CoroutineExceptionHandler { context, throwable ->
        localHandler.handleException(context, throwable)
        globalHandlers.handleException(context, throwable)
    }
}

private fun List<CoroutineExceptionHandler>.handleException(context: CoroutineContext, throwable: Throwable) {
    forEach { it.handleException(context, throwable) }
}