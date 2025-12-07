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
import androidx.compose.runtime.retain.retain
import com.nlab.statekit.foundation.store.StoreMaterialScope
import com.nlab.statekit.store.Store

/**
 * @author Doohyun
 */
@Composable
fun <A : Any, S : Any> retained(calculation: StoreMaterialScope.() -> Store<A, S>): Store<A, S> {
    val storeMaterialScope = retainStoreMaterialScope()
    return retain { storeMaterialScope.calculation() }
}

@Composable
fun <A : Any, S : Any> retainStore(
    vararg keys: Any?,
    calculation: StoreMaterialScope.() -> Store<A, S>
): Store<A, S> {
    val storeMaterialScope = retainStoreMaterialScope(keys = keys)
    return retain(storeMaterialScope) { storeMaterialScope.calculation() }
}