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
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.RippleDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Extended Theme for Reminder.
 * @author Doohyun
 */
object PlaneatTheme {
    val colors: PlaneatColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalPlaneatColorScheme.current

    val typography: PlaneatTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalPlaneatTypography.current

    val extraFont: PlaneatExtraFont
        @Composable
        @ReadOnlyComposable
        get() = LocalPlaneatExtraFont.current
}

@Composable
fun PlaneatTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val planeatColorScheme =
        if (isDarkTheme) DarkPlaneatColorScheme
        else LightPlaneatColorScheme
    CompositionLocalProvider(LocalPlaneatColorScheme provides planeatColorScheme) {
        MaterialTheme {
            val customTextSelectionColors = TextSelectionColors(
                handleColor = PlaneatTheme.colors.contentTextSelection,
                backgroundColor = PlaneatTheme.colors.bgTextSelection
            )
            val customRippleConfiguration = RippleConfiguration(
                color = PlaneatTheme.colors.bgRipple,
                rippleAlpha = RippleDefaults.RippleAlpha
            )
            CompositionLocalProvider(
                LocalTextSelectionColors provides customTextSelectionColors,
                LocalRippleConfiguration provides customRippleConfiguration
            ) {
                content()
            }
        }
    }
}