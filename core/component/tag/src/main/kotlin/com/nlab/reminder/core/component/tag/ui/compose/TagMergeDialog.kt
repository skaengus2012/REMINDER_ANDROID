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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.designsystem.compose.component.PlaneatDialog
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.translation.StringIds

/**
 * @author Doohyun
 */
@Composable
internal fun TagMergeDialog(
    fromTagName: NonBlankString,
    toTagName: NonBlankString,
    onDismissRequested: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    PlaneatDialog(onDismissRequest = onDismissRequested) {
        Column(
            modifier = Modifier.width(250.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(start = 15.dp, top = 15.dp, end = 15.dp),
                text = stringResource(StringIds.tag_merge),
                style = PlaneatTheme.typography
                    .bodyLarge
                    .copy(fontWeight = FontWeight.Bold),
                color = PlaneatTheme.colors.content1
            )
            Text(
                modifier = Modifier.padding(start = 15.dp, top = 2.5.dp, end = 15.dp, bottom = 15.dp),
                text = stringResource(
                    StringIds.tag_merge_dialog_description,
                    fromTagName.value,
                    toTagName.value
                ),
                style = PlaneatTheme.typography.bodySmall,
                color = PlaneatTheme.colors.content1,
                textAlign = TextAlign.Center
            )
            TagDialogButtons(
                onCancel = onCancel,
                onConfirm = onConfirm
            )
        }
    }
}

@Previews
@Composable
private fun TagMergeDialogPreview() {
    PlaneatTheme {
        Box(modifier = Modifier.size(300.dp)) {
            TagMergeDialog(
                fromTagName = "A".toNonBlankString(),
                toTagName = "B".toNonBlankString(),
                onDismissRequested = {},
                onCancel = {},
                onConfirm = {}
            )
        }
    }
}