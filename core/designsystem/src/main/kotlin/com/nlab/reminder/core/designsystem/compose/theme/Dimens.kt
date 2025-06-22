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

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @author Doohyun
 */
fun Modifier.horizontalMediumPadding(
    top: Dp = 0.dp,
    boolean: Dp = 0.dp
): Modifier = composed(debugInspectorInfo { name = "horizontalMediumPadding" }) {
    val horizontalPadding = dimensionResource(DimenIds.horizontal_padding_medium)
    padding(
        start = horizontalPadding,
        top = top,
        end = horizontalPadding,
        bottom = boolean
    )
}