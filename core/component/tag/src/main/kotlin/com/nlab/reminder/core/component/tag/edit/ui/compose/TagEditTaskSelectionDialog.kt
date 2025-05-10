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

package com.nlab.reminder.core.component.tag.edit.ui.compose

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.androidx.compose.ui.throttleClick
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.designsystem.compose.component.PlaneatDialog
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.translation.PluralsIds
import com.nlab.reminder.core.translation.StringIds

/**
 * @author Doohyun
 */
@Composable
internal fun TagEditTaskSelectionDialog(
    tagName: NonBlankString,
    onDismissRequest: () -> Unit,
    onRenameRequestClicked: () -> Unit,
    onDeleteRequestClicked: () -> Unit
) {
    PlaneatDialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .width(250.dp)
                .padding(vertical = 17.dp)
                .wrapContentHeight()
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                text = stringResource(StringIds.format_tag, tagName.value),
                style = PlaneatTheme.typography.titleSmall,
                color = PlaneatTheme.colors.content1,
                textAlign = TextAlign.Center
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = 17.dp),
                thickness = 0.5.dp,
                color = PlaneatTheme.colors.bgLine1,
            )
            TagEditTaskSelectionDialogButton(
                text = stringResource(StringIds.tag_rename),
                fontColor = PlaneatTheme.colors.content1,
                onClick = onRenameRequestClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )
            TagEditTaskSelectionDialogButton(
                text = pluralStringResource(PluralsIds.tag_delete, count = 1, 1),
                onClick = onDeleteRequestClicked,
                fontColor = PlaneatTheme.colors.red1,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
        }
    }
}

@Composable
private fun TagEditTaskSelectionDialogButton(
    text: String,
    fontColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        TagEditTaskSelectionDialogButtonBackground(
            onClick = onClick,
            onClickLabel = text
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 7.5.dp),
            text = text,
            style = PlaneatTheme.typography.bodyMedium,
            color = fontColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BoxScope.TagEditTaskSelectionDialogButtonBackground(
    onClick: () -> Unit,
    onClickLabel: String
) {
    Spacer(
        modifier = Modifier
            .matchParentSize()
            .combinedClickable(
                remember { MutableInteractionSource() },
                indication = ripple(color = PlaneatTheme.colors.bgRipple1),
                onClick = throttleClick(onClick = onClick),
                onClickLabel = onClickLabel,
                role = Role.Button
            )
    )
}

@Previews
@Composable
private fun TagEditTaskSelectionDialogPreview() {
    PlaneatTheme {
        Box(modifier = Modifier.size(300.dp)) {
            TagEditTaskSelectionDialog(
                tagName = "Hello, tag edit dialog".toNonBlankString(),
                onDismissRequest = {},
                onRenameRequestClicked = {},
                onDeleteRequestClicked = {}
            )
        }
    }
}