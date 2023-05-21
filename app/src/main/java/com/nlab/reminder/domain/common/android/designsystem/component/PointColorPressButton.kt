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

package com.nlab.reminder.domain.common.android.designsystem.component

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nlab.reminder.domain.common.android.designsystem.theme.ReminderTheme

/**
 * @author Doohyun
 */
@Composable
fun PointColorPressButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable (contentColor: Color) -> Unit
) {
    InternalPointColorPressButton(
        modifier = modifier,
        onClick = onClick,
    ) { interactionSource ->
        val isPressed by interactionSource.collectIsPressedAsState()
        content(
            contentColor = ReminderTheme.colors.pointColor1.copy(alpha = if (isPressed) 0.5f else 1f)
        )
    }
}

@Composable
private fun InternalPointColorPressButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable (InteractionSource) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) { content(interactionSource) }
}

@Preview(
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Composable
fun PointerColorPressButtonPreview() {
    ReminderTheme {
        PointColorPressButton(
            modifier = Modifier.padding(10.dp)
        ) { contentColor ->
            Text(
                text = "Button",
                color = contentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
