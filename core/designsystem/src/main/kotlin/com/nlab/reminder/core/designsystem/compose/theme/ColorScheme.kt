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
import androidx.compose.ui.graphics.Color

/**
 * Theme color set used in Reminder.
 * @author Doohyun
 */
@Immutable
data class PlaneatColorScheme(
    val bg1: Color,
    val bg2: Color,
    val bgRipple1: Color,
    val bgCard1: Color,
    val bgCard2: Color,
    val bgCardStroke1: Color,
    val bgLine1: Color,
    val bgLine2: Color,
    val bgPlaceHolder1: Color,
    val content1: Color,
    val content2: Color,
    val content2Hint: Color,
    val content3: Color,
    // Compose Only
    val bgCard1Ripple: Color,
    val bgDialogSurface: Color,
    val bgTag: Color,
    val bgTagRipple: Color,
    val contentTag: Color,
    // Colors that do not change depending on the theme.
    val point1: Color = Point1,
    val point2: Color = Point2,
    val point3: Color = Point3,
    val red1: Color = Red400,
    val white: Color = Color.White,
    val black: Color = Color.Black
)

internal val LocalPlaneatColorScheme = staticCompositionLocalOf {
    PlaneatColorScheme(
        bg1 = Color.Unspecified,
        bg2 = Color.Unspecified,
        bgRipple1 = Color.Unspecified,
        bgCard1 = Color.Unspecified,
        bgCard2 = Color.Unspecified,
        bgCardStroke1 = Color.Unspecified,
        bgLine1 = Color.Unspecified,
        bgLine2 = Color.Unspecified,
        bgPlaceHolder1 = Color.Unspecified,
        content1 = Color.Unspecified,
        content2 = Color.Unspecified,
        content2Hint = Color.Unspecified,
        content3 = Color.Unspecified,
        // Compose Only
        bgCard1Ripple = Color.Unspecified,
        bgDialogSurface = Color.Unspecified,
        bgTag = Color.Unspecified,
        bgTagRipple = Color.Unspecified,
        contentTag = Color.Unspecified,
    )
}

internal val LightPlaneatColorScheme = PlaneatColorScheme(
    bg1 = Bg1Light,
    bg2 = Bg2Light,
    bgRipple1 = BgRipple1Light,
    bgCard1 = BgCard1Light,
    bgCard2 = BgCard2Light,
    bgCardStroke1 = BgCardStroke1Light,
    bgLine1 = BgLine1Light,
    bgLine2 = BgLine2Light,
    bgPlaceHolder1 = BgPlaceHolder1Light,
    content1 = Content1Light,
    content2 = Content2Light,
    content2Hint = Content2HintLight,
    content3 = Content3Light,
    // Compose Only
    bgCard1Ripple = BgCard1RippleLight,
    bgDialogSurface = BgDialogSurfaceLight,
    bgTag = BgTagLight,
    bgTagRipple = BgTagRippleLight,
    contentTag = ContentTagLight,
)

internal val DarkPlaneatColorScheme = PlaneatColorScheme(
    bg1 = Bg1Dark,
    bg2 = Bg2Dark,
    bgRipple1 = BgRipple1Dark,
    bgCard1 = BgCard1Dark,
    bgCard2 = BgCard2Dark,
    bgCardStroke1 = BgCardStroke1Light,
    bgLine1 = BgLine1Dark,
    bgLine2 = BgLine2Dark,
    bgPlaceHolder1 = BgPlaceHolder1Light,
    content1 = Content1Dark,
    content2 = Content2Dark,
    content2Hint = Content2DarkLight,
    content3 = Content3Dark,
    point1 = Point1,
    point2 = Point2,
    point3 = Point3,
    red1 = Red400,
    // Compose Only
    bgCard1Ripple = BgCard1RippleDark,
    bgDialogSurface = BgDialogSurfaceDark,
    bgTag = BgTagDark,
    bgTagRipple = BgTagRippleDark,
    contentTag = ContentTagDark,
)