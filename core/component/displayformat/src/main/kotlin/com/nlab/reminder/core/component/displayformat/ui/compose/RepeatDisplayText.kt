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

package com.nlab.reminder.core.component.displayformat.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalResources
import com.nlab.reminder.core.data.model.Repeat
import kotlinx.datetime.LocalDate
import com.nlab.reminder.core.component.displayformat.ui.repeatDisplayText as repeatDisplayTextOrigin

/**
 * Generates a localized display string for a given [Repeat] on Compose.
 *
 * @author Doohyun
 * @see [com.nlab.reminder.core.component.displayformat.ui.repeatDisplayText]
 */
@ReadOnlyComposable
@Composable
fun repeatDisplayText(
    repeat: Repeat,
    triggerAt: LocalDate
): String {
    val resources = LocalResources.current
    return repeatDisplayTextOrigin(
        resources = resources,
        repeat = repeat,
        triggerAt = triggerAt
    )
}