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

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * @author Doohyun
 */
@Immutable
data class ReminderTypography(
    val titleMedium: TextStyle = createTextStyle(
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
    ),
    val bodyLarge: TextStyle = createTextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    ),
    val bodyMedium: TextStyle = createTextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    ),
    val bodySmall: TextStyle = createTextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    )
)

internal val LocalReminderTypography = staticCompositionLocalOf { ReminderTypography() }

private fun createTextStyle(
    fontSize: TextUnit,
    fontWeight: FontWeight,
): TextStyle = TextStyle(
    fontSize = fontSize,
    fontWeight = fontWeight,
    platformStyle = PlatformTextStyle(
        includeFontPadding = false
    ),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)