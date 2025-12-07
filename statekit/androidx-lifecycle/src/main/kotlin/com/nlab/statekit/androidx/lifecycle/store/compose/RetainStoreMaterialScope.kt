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

package com.nlab.statekit.androidx.lifecycle.store.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import com.nlab.statekit.foundation.store.StoreMaterialScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @author Thalys
 */
@Composable
internal fun retainStoreMaterialScope(): StoreMaterialScope {
    val coroutineScope = retain { createBaseCoroutineScope() }
    RetainedEffect(Unit) {
        onRetire { coroutineScope.cancel() }
    }
    return retain { StoreMaterialScope(baseCoroutineScope = coroutineScope) }
}

@Composable
internal fun retainStoreMaterialScope(vararg keys: Any?): StoreMaterialScope {
    val coroutineScope = retain(keys = keys) { createBaseCoroutineScope() }
    RetainedEffect(coroutineScope) {
        onRetire { coroutineScope.cancel() }
    }
    return retain(coroutineScope) { StoreMaterialScope(baseCoroutineScope = coroutineScope) }
}

private fun createBaseCoroutineScope(): CoroutineScope {
    /**
     * For dispatcher settings, refer to the ViewModel function.
     * @see androidx.lifecycle.viewmodel.internal.createViewModelScope
     */
    val dispatcher = try {
        // In platforms where `Dispatchers.Main` is not available, Kotlin Multiplatform will
        // throw
        // an exception (the specific exception type may depend on the platform). Since there's
        // no
        // direct functional alternative, we use `EmptyCoroutineContext` to ensure that a
        // coroutine
        // launched within this scope will run in the same context as the caller.
        Dispatchers.Main.immediate
    } catch (_: NotImplementedError) {
        // In Native environments where `Dispatchers.Main` might not exist (e.g., Linux):
        EmptyCoroutineContext
    } catch (_: IllegalStateException) {
        // In JVM Desktop environments where `Dispatchers.Main` might not exist (e.g., Swing):
        EmptyCoroutineContext
    }
    return CoroutineScope(context = dispatcher + SupervisorJob())
}