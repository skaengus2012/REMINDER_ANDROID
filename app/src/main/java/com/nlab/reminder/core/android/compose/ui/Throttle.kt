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

package com.nlab.reminder.core.android.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.*
import com.nlab.reminder.core.kotlin.coroutine.flow.throttleFirst
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * @author Doohyun
 */
@Composable
fun (() -> Unit).throttle(
    windowDuration: Long = 100
): () -> Unit {
    val clickEvent = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }
    LaunchedEffect(this, windowDuration) {
        clickEvent
            .throttleFirst(windowDuration)
            .collect { invoke() }
    }

    return { clickEvent.tryEmit(Unit) }
}