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

package com.nlab.reminder.core.component.toolbar.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.androidx.compose.ui.ColorPressButton
import com.nlab.reminder.core.androidx.compose.ui.throttleClick
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme
import com.nlab.reminder.core.translation.StringIds

/**
 * @author Thalys
 */
@Composable
fun CompleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ColorPressButton(
        modifier = modifier.toolbarHeight().padding(horizontal = 10.dp),
        contentColor = PlaneatTheme.colors.point1,
        onClick = throttleClick(onClick = onClick),
    ) { color ->
        Text(
            text = stringResource(StringIds.label_complete),
            color = color,
            style = PlaneatTheme.typography
                .bodyLarge
                .copy(fontWeight = FontWeight.Bold)
        )
    }
}


@Previews
@Composable
private fun CompleteButtonPreviews() {
    PlaneatTheme {
        Box(
            modifier = Modifier
                .background(PlaneatTheme.colors.bg1)
                .size(60.dp)
        ) {
            CompleteButton(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(color = PlaneatTheme.colors.red1),
                onClick = {}
            )
        }
    }
}