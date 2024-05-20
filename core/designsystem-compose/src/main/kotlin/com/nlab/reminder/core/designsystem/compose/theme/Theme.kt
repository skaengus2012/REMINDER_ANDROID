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

package com.nlab.reminder.core.designsystem.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Extended Theme for Reminder.
 * @author Doohyun
 */
object ReminderTheme {
    val colors: ReminderColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalReminderColorScheme.current

    val typography: ReminderTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalReminderTypography.current
}

@Immutable
private object DefaultRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = LocalContentColor.current

    @Composable
    override fun rippleAlpha() = RippleAlpha(
        pressedAlpha = 0.5f,
        focusedAlpha = 0.5f,
        draggedAlpha = 0.5f,
        hoveredAlpha = 0.5f,
    )
}

@Composable
fun ReminderTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val reminderColorScheme = if (isDarkTheme) DarkReminderColorScheme else LightReminderColorScheme
    MaterialTheme {
        CompositionLocalProvider(
            LocalReminderColorScheme provides reminderColorScheme,
            LocalRippleTheme provides DefaultRippleTheme,
            content = content
        )
    }
}