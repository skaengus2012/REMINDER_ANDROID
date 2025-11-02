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

package com.nlab.reminder.core.androidx.compose.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus

/**
 * @author Doohyun
 */
@Composable
fun <T, R> StateFlow<T>.rememberAccumulatedStateStream(
    accumulator: (prev: R, next: T) -> R,
    initialValueTransform: (T) -> R,
): StateFlow<R> {
    val coroutineScope = rememberCoroutineScope()
    return remember(this, coroutineScope) {
        val initialValue = initialValueTransform(value)
        scan(initial = initialValue, operation = accumulator).stateIn(
            scope = coroutineScope + Dispatchers.Default,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = initialValue
        )
    }
}