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

import androidx.compose.ui.graphics.Color

/**
 * sync with [./main/res/values/color.xml]
 * @author Doohyun
 */

// gray
internal val Gray2 = Color(0xFF838387)
// https://m2.material.io/design/color/the-color-system.html#tools-for-picking-colors
internal val Gray400 = Color(0xFFBDBDBD)
internal val Gray500 = Color(0xFF9E9E9E)
internal val Gray600 = Color(0xFF757575)
internal val Gray700 = Color(0xFF616161)
internal val Gray800 = Color(0xFF424242)

// red
// https://m2.material.io/design/color/the-color-system.html#tools-for-picking-colors
internal val Red400 = Color(0xFFFF1744)

// point
internal val Point1 = Color(0xFF1CCF98)
internal val Point1Sub = Color(0xFF7FD4AE)
internal val Point2 = Color(0xFFBE94F3)
internal val Point3 = Color(0xFFFFB631)
internal val Point4 = Color(0xFF5E9BFF)

// light
internal val Bg1Light = Color(0xFFF2F2F6)
internal val Bg1LayerLight = Color.White
internal val Bg1LayerRippleLight = Color(0xFFD3D3D3)
internal val Bg2Light = Color.White
internal val Bg2LayerLight = Color(0xFFF8F8FA)
internal val Bg2CardLight = Color(0xFFEEEEEE)
internal val Bg2CardStrokeLight = Color(0xFFD3D3D3)
internal val BgLine1Light = Gray400
internal val BgLine2Light = Gray2
internal val BgPlaceHolder1Light = Color(0xFFD3D3D3)
internal val BgRippleLight = Color.Black.copy(alpha = 0.12f)
internal val BgTextSelectionLight = Point1.copy(alpha = 0.2f)
internal val Content1Light = Color(0xFF393939)
internal val Content2Light = Color(0xFF8A8A8D)
internal val Content2HintLight = Content2Light.copy(alpha = 0.6f)
internal val Content3Light = Gray500
internal val Content3HintLight = Gray500.copy(alpha = 0.7f)
// light (compose only)
internal val BgDialogSurfaceLight = Color(0xFFF0F0F0)
internal val BgDialogInputLight = Color.White
internal val BgTagLight = Color(0xFFEEEEEE)
internal val BgTagRippleLight = Color(0xFFBDBDBD)
internal val ContentTagLight = Gray2

// dark
internal val Bg1Dark = Color.Black
internal val Bg1LayerDark = Color(0xFF1C1C1C)
internal val Bg1LayerRippleDark = Color.White.copy(alpha = 0.16f)
internal val Bg2Dark = Color.Black
internal val Bg2LayerDark = Bg1LayerDark
internal val Bg2CardDark = Gray800
internal val Bg2CardStrokeDark = Color(0xFFEEEEEE)
internal val BgLine1Dark = Gray800
internal val BgLine2Dark = Gray2
internal val BgPlaceHolder1Dark = Gray700
internal val BgRippleDark = Color.White.copy(alpha = 0.16f)
internal val BgTextSelectionDark = Point1.copy(alpha = 0.3f)
internal val Content1Dark = Color.White
internal val Content2Dark = Color(0xFFB7B7B7)
internal val Content2HintDark = Content2Dark.copy(alpha = 0.6f)
internal val Content3Dark = Gray500
internal val Content3HintDark = Gray500.copy(alpha = 0.7f)
// dark(compose only)
internal val BgDialogSurfaceDark = Color(0xFF242525)
internal val BgDialogInputDark = Color(0xFF1C1C1D)
internal val BgTagDark = Color(0xFF2F2F2F)
internal val BgTagRippleDark = BgRippleDark
internal val ContentTagDark = Color(0xFF838387)