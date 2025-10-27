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
    val bg1Layer: Color,
    val bg1LayerRipple: Color,
    val bg2: Color,
    val bg2Layer: Color,
    val bg2Card: Color,
    val bg2CardStroke: Color,
    val bgLine1: Color,
    val bgLine2: Color,
    val bgPlaceHolder1: Color,
    val bgRipple: Color,
    val bgTextSelection: Color,
    val content1: Color,
    val content2: Color,
    val content2Hint: Color,
    val content3: Color,
    val content3Hint: Color,

    // Compose Only
    val bgDialogSurface: Color,
    val bgDialogInput: Color,
    val bgTag: Color,
    val bgTagRipple: Color,
    val contentTag: Color,

    // Colors that do not change depending on the theme.
    val contentTextSelection: Color = Point1,
    val point1: Color = Point1,
    val point1Sub: Color = Point1Sub,
    val point2: Color = Point2,
    val point3: Color = Point3,
    val point4: Color = Point4,
    val red1: Color = Red400,
    val white: Color = Color.White,
    val black: Color = Color.Black,
)

internal val LocalPlaneatColorScheme = staticCompositionLocalOf {
    PlaneatColorScheme(
        bg1 = Color.Unspecified,
        bg1Layer = Color.Unspecified,
        bg1LayerRipple = Color.Unspecified,
        bg2 = Color.Unspecified,
        bg2Layer = Color.Unspecified,
        bg2Card = Color.Unspecified,
        bg2CardStroke = Color.Unspecified,
        bgLine1 = Color.Unspecified,
        bgLine2 = Color.Unspecified,
        bgPlaceHolder1 = Color.Unspecified,
        bgRipple = Color.Unspecified,
        bgTextSelection = Color.Unspecified,
        content1 = Color.Unspecified,
        content2 = Color.Unspecified,
        content2Hint = Color.Unspecified,
        content3 = Color.Unspecified,
        content3Hint = Color.Unspecified,
        // Compose Only
        bgDialogSurface = Color.Unspecified,
        bgDialogInput = Color.Unspecified,
        bgTag = Color.Unspecified,
        bgTagRipple = Color.Unspecified,
        contentTag = Color.Unspecified,
    )
}

internal val LightPlaneatColorScheme = PlaneatColorScheme(
    bg1 = Bg1Light,
    bg1Layer = Bg1LayerLight,
    bg1LayerRipple = Bg1LayerRippleLight,
    bg2 = Bg2Light,
    bg2Layer = Bg2LayerLight,
    bg2Card = Bg2CardLight,
    bg2CardStroke = Bg2CardStrokeLight,
    bgRipple = BgRippleLight,
    bgLine1 = BgLine1Light,
    bgLine2 = BgLine2Light,
    bgPlaceHolder1 = BgPlaceHolder1Light,
    bgTextSelection = BgTextSelectionLight,
    content1 = Content1Light,
    content2 = Content2Light,
    content2Hint = Content2HintLight,
    content3 = Content3Light,
    content3Hint = Content3HintLight,
    // Compose Only
    bgDialogSurface = BgDialogSurfaceLight,
    bgDialogInput = BgDialogInputLight,
    bgTag = BgTagLight,
    bgTagRipple = BgTagRippleLight,
    contentTag = ContentTagLight,
)

internal val DarkPlaneatColorScheme = PlaneatColorScheme(
    bg1 = Bg1Dark,
    bg1Layer = Bg1LayerDark,
    bg1LayerRipple = Bg1LayerRippleDark,
    bg2 = Bg2Dark,
    bg2Layer = Bg2LayerDark,
    bg2CardStroke = Bg2CardStrokeDark,
    bg2Card = Bg2CardDark,
    bgLine1 = BgLine1Dark,
    bgLine2 = BgLine2Dark,
    bgPlaceHolder1 = BgPlaceHolder1Dark,
    bgRipple = BgRippleDark,
    bgTextSelection = BgTextSelectionDark,
    content1 = Content1Dark,
    content2 = Content2Dark,
    content2Hint = Content2HintDark,
    content3 = Content3Dark,
    content3Hint = Content3HintDark,
    // Compose Only
    bgDialogSurface = BgDialogSurfaceDark,
    bgDialogInput = BgDialogInputDark,
    bgTag = BgTagDark,
    bgTagRipple = BgTagRippleDark,
    contentTag = ContentTagDark,
)