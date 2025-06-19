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

package com.nlab.reminder.core.androidx.compose.ui

import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.designsystem.compose.theme.DimenIds
import com.nlab.reminder.core.designsystem.compose.theme.DrawableIds
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme

/**
 * @author Thalys
 */
@Composable
fun IconButton(
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    indication: Indication? = null,
) {
    Box(modifier = modifier.size(dimensionResource(DimenIds.icon_button_size))) {
        ButtonBackground(
            onClick = onClick,
            onClickLabel = contentDescription,
            indication = indication
        )

        Icon(
            modifier = Modifier.align(Alignment.Center),
            painter = painter,
            tint = tint,
            contentDescription = null
        )
    }
}

@Previews
@Composable
private fun IconButtonPreview() {
    PlaneatTheme {
        Box(modifier = Modifier.background(PlaneatTheme.colors.bg1)) {
            IconButton(
                onClick = {},
                painter = painterResource(DrawableIds.ic_new_plan),
                tint = PlaneatTheme.colors.red1,
                contentDescription = null
            )
        }
    }
}