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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nlab.statekit.androidx.lifecycle.store.RetainedStoreFactoryViewModel
import com.nlab.statekit.foundation.store.StoreMaterialScope
import com.nlab.statekit.store.Store
import kotlin.uuid.Uuid

/**
 * @author Doohyun
 */
@Composable
fun <A : Any, S : Any> retainedStore(
    key: Any,
    block: StoreMaterialScope.() -> Store<A, S>
): Store<A, S> {
    val viewModel: RetainedStoreFactoryViewModel = viewModel()
    val result = remember(key) {
        viewModel.getOrPut(key, block)
    }
    return result
}

@Composable
fun <A : Any, S : Any> retainedStore(block: StoreMaterialScope.() -> Store<A, S>): Store<A, S> {
    val uniqueId = rememberSaveable { Uuid.random() }
    return retainedStore(key = uniqueId, block = block)
}