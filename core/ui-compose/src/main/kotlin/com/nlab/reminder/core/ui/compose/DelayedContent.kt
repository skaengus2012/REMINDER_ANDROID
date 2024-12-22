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

package com.nlab.reminder.core.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
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
fun DelayedContent(
    delayTimeMillis: Long,
    visibleState: DelayedContentState = rememberDelayedContentState(),
    content: @Composable () -> Unit
) {
    if (visibleState.isVisible) {
        content()
    } else {
        LaunchedEffect(Unit) {
            delay(delayTimeMillis)
            visibleState.setVisible()
        }
    }
}

@Composable
fun rememberDelayedContentState(visible: Boolean = false): DelayedContentState =
    rememberSaveable(visible, saver = DelayedContentState.Saver()) { DelayedContentState(initial = visible) }

@Stable
class DelayedContentState internal constructor(initial: Boolean) {
    var isVisible: Boolean by mutableStateOf(initial)
        private set

    internal fun setVisible() {
        isVisible = true
    }

    companion object {
        fun Saver() = Saver<DelayedContentState, Boolean>(
            save = { it.isVisible },
            restore = { DelayedContentState(it) }
        )
    }
}