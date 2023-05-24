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

package com.nlab.reminder.domain.common.android.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * @author Doohyun
 */
// /app/src/main/res/values/colors.xml
internal val Gray1 = Color(0xFFF2F2F6)
internal val Gray2 = Color(0xFF838387)
internal val Gray400 = Color(0xFFBDBDBD)
internal val Gray800 = Color(0xFF424242)

internal val Bg1Light = Gray1
internal val Bg2Light = Color.White
internal val BgRipple1Light = Color(0xFFCBCBCB)
internal val BgCard1Light = Color.White
internal val BgCard1RippleLight = Color(0xFFEBEBEB)
internal val BgLine1Light = Gray400
internal val BgDialogSurfaceLight = Color(0xFFF0F0F0)
internal val BgTagLight = Color(0xFFEEEEEE)
internal val BgTagRippleLight = BgRipple1Light
internal val Font1Light = Color(0xFF393939)
internal val Font2Light = Color(0xFF8A8A8D)
internal val FontTagLight = Gray2

internal val Bg1Dark = Color.Black
internal val Bg2Dark = Color(0xFF1C1C1D)
internal val BgRipple1Dark =  Color(0xFF49494C)
internal val BgCard1Dark = Color(0xFF1C1C1C)
internal val BgCard1RippleDark = BgRipple1Dark
internal val BgDialogSurfaceDark = Color(0xFF242525)
internal val BgLine1Dark = Gray800
internal val BgTagDark = Color(0xFF2F2F2F)
internal val BgTagRippleDark = BgRipple1Dark
internal val Font1Dark = Color.White
internal val Font2Dark = Color(0xFFB7B7B7)
internal val FontTagDark = Color(0xFF838387)

internal val BgDim = Color.Black.copy(alpha = 0.5f)
internal val BgLine2 = Gray2
internal val PointColor1 = Color(0xFF1CCF98)