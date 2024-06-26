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

package com.nlab.reminder.domain.common.tag.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nlab.reminder.R
import com.nlab.reminder.core.androidx.compose.ui.throttle
import com.nlab.reminder.core.designsystem.compose.theme.ReminderTheme
import com.nlab.reminder.core.data.model.TagUsageCount

/**
 * @author Doohyun
 */
@Composable
fun TagDeleteBottomSheetContent(
    tagName: String,
    usageCount: TagUsageCount,
    modifier: Modifier = Modifier,
    onConfirmClicked: () -> Unit = {},
    onCancelClicked: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = usageCount.mapToString(
                transform = { pluralStringResource(id = R.plurals.tag_delete, count = it, it) },
                transformWhenOverflow = { stringResource(id = R.string.tag_delete_overflow, it) }
            ),
            style = ReminderTheme.typography.bodySmall,
            color = ReminderTheme.colors.content1,
            textAlign = TextAlign.Center
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, top = 6.dp, end = 30.dp, bottom = 22.dp),
            style = ReminderTheme.typography.bodySmall,
            text = usageCount.mapToString(
                transform = {
                    pluralStringResource(id = R.plurals.tag_delete_dialog_description, count = it, tagName, it)
                },
                transformWhenOverflow = {
                    stringResource(id = R.string.tag_delete_dialog_description_overflow, tagName, it)
                }
            ),
            color = ReminderTheme.colors.content1,
            textAlign = TextAlign.Center
        )

        HorizontalDivider(
            thickness = 0.5.dp,
            color = ReminderTheme.colors.bgLine1,
        )

        InternalButton(
            text = LocalContext.current.getString(R.string.delete),
            fontColor = ReminderTheme.colors.red1,
            onClick = onConfirmClicked
        )

        InternalButton(
            text = LocalContext.current.getString(R.string.cancel),
            fontColor = ReminderTheme.colors.content2,
            onClick = onCancelClicked,
            modifier = Modifier.padding(bottom = 10.dp)
        )
    }
}

@Composable
private fun InternalButton(
    text: String,
    fontColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick.throttle(),
                onClickLabel = text,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = ReminderTheme.colors.bgRipple1)
            ),
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            text = text,
            textAlign = TextAlign.Center,
            color = fontColor,
            style = ReminderTheme.typography.bodyLarge,
        )
    }
}

@Preview(
    name = "LightTagDeleteBottomSheetContentPreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkTagDeleteBottomSheetContentPreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun TagDeleteBottomSheetContentPreview() {
    ReminderTheme {
        Box(modifier = Modifier.background(color = ReminderTheme.colors.bgDialogSurface)) {
            TagDeleteBottomSheetContent(
                tagName = "Hello TagDeleteBottomSheetContentPreview",
                usageCount = TagUsageCount(1)
            )
        }
    }
}