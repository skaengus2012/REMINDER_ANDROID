/*
 * Copyright (C) 2026 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.schedulelist.bottombar.ui

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.nlab.reminder.core.component.bottomappbar.ui.BottomAppbarState
import com.nlab.reminder.core.component.bottomappbar.ui.MutableBottomAppbarState

/**
 * @author Doohyun
 */
@Stable
class ScheduleListBottomAppbarState internal constructor(
    @FloatRange(from = 0.0, to = 1.0) initialBackgroundAlpha: Float = 0f
) : BottomAppbarState {
    private val delegate = MutableBottomAppbarState(initialBackgroundAlpha)

    @get:FloatRange(from = 0.0, to = 1.0)
    override var backgroundAlpha: Float
        get() = delegate.backgroundAlpha
        internal set(value) {
            delegate.backgroundAlpha = value
        }
}

@Composable
fun rememberScheduleListBottomAppbarState(
    @FloatRange(from = 0.0, to = 1.0) initialBackgroundAlpha: Float = 0f
): ScheduleListBottomAppbarState = remember {
    ScheduleListBottomAppbarState(initialBackgroundAlpha)
}
