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

package com.nlab.reminder.core.android.compose.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * @author Doohyun
 */
@Composable
fun DelayedVisibleContent(
    delayTimeMillis: Long,
    visibleState: DelayedVisibleState,
    key: Any?,
    content: @Composable () -> Unit
) {
    if (visibleState.value) {
        content()
    }

    if (visibleState.value.not()) {
        LaunchedEffect(key) {
            delay(delayTimeMillis)
            visibleState.visible()
        }
    }
}

class DelayedVisibleState internal constructor(initial: Boolean) {
    var value: Boolean by mutableStateOf(initial)
        private set

    internal fun visible() {
        value = true
    }

    companion object {
        internal val Saver: Saver<DelayedVisibleState, *> = Saver(
            save = { it.value },
            restore = { DelayedVisibleState(it) }
        )
    }
}

@Composable
fun rememberDelayedVisibleState(visible: Boolean = false): DelayedVisibleState {
    return rememberSaveable(saver = DelayedVisibleState.Saver) { DelayedVisibleState(initial = visible) }
}