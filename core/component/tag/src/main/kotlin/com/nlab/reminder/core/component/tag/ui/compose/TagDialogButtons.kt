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

package com.nlab.reminder.core.component.tag.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.ui.compose.ColorPressButton
import com.nlab.reminder.core.ui.compose.throttleClick
import com.nlab.reminder.core.ui.compose.tooling.preview.Previews
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme
import com.nlab.reminder.core.translation.StringIds

/**
 * This function must be used in Column.
 *
 * @author Doohyun
 */
@Composable
internal fun TagDialogButtons(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = PlaneatTheme.colors.bgLine1
    )
    Row(modifier = Modifier.fillMaxWidth().height(40.dp)) {
        TagDialogButton(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            label = stringResource(StringIds.cancel),
            onClick = onCancel
        )
        VerticalDivider(
            thickness = 0.5.dp,
            color = PlaneatTheme.colors.bgLine1
        )
        TagDialogButton(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            label = stringResource(StringIds.ok),
            onClick = onConfirm
        )
    }
}

@Composable
private fun TagDialogButton(
    modifier: Modifier,
    label: String,
    onClick: () -> Unit
) {
    ColorPressButton(
        modifier = modifier,
        contentColor = PlaneatTheme.colors.point1,
        onClick = throttleClick(onClick = onClick)
    ) { contentColor ->
        Text(
            modifier = Modifier.fillMaxWidth(),
            color = contentColor,
            text = label,
            style = PlaneatTheme.typography
                .bodyLarge
                .copy(textAlign = TextAlign.Center)
        )
    }
}

@Previews
@Composable
private fun TagDialogButtonsPreviews() {
    PlaneatTheme {
        Column {
            Spacer(modifier = Modifier.height(10.dp))
            TagDialogButtons(
                onCancel = {},
                onConfirm = {}
            )
        }
    }
}