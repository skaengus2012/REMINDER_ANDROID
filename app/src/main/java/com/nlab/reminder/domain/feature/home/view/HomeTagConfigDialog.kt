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

package com.nlab.reminder.domain.feature.home.view

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nlab.reminder.R
import com.nlab.reminder.domain.common.android.designsystem.component.ReminderThemeDialog
import com.nlab.reminder.domain.common.android.designsystem.component.throttle
import com.nlab.reminder.domain.common.android.designsystem.theme.ReminderTheme

/**
 * @author Doohyun
 */
@Composable
internal fun HomeTagConfigDialog(
    tagName: String,
    usageCount: Int,
    onDismiss: () -> Unit = {},
    onRenameRequestClicked: () -> Unit = {},
    onDeleteRequestClicked: () -> Unit = {}
) {
    ReminderThemeDialog(onDismiss) {
        Column(
            modifier = Modifier
                .width(250.dp)
                .padding(vertical = 15.dp)
                .wrapContentHeight()
        ) {
            Text(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                text = LocalContext.current.getString(R.string.format_tag, tagName),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 13.sp,
                color = ReminderTheme.colors.font1,
                textAlign = TextAlign.Center
            )
            HomeTagConfigButton(
                text = LocalContext.current.getString(R.string.tag_rename),
                onClick = onRenameRequestClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 10.dp)
            )
            HomeTagConfigButton(
                text = LocalContext.current.resources.getQuantityString(R.plurals.tag_delete, usageCount, usageCount),
                onClick =  onDeleteRequestClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
        }
    }
}

@Composable
private fun HomeTagConfigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        HomeTagConfigButtonBackground(
            onClick = onClick.throttle(),
            onClickLabel = text
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = ReminderTheme.colors.font1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 7.5.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BoxScope.HomeTagConfigButtonBackground(
    onClick: () -> Unit,
    onClickLabel: String
) {
    val interactiveSource = remember { MutableInteractionSource() }
    Spacer(
        modifier = Modifier
            .matchParentSize()
            .combinedClickable(
                interactiveSource,
                indication = rememberRipple(color = ReminderTheme.colors.bgRipple1),
                onClick = onClick.throttle(),
                onClickLabel = onClickLabel,
                role = Role.Button
            )
    )
}

@Preview(
    name = "LightHomeTagConfigDialogPreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkHomeTagConfigDialogPreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun HomeTagConfigDialogPreview() {
    ReminderTheme {
        HomeTagConfigDialog(
            tagName = "Hello HomeTag Config DialogPreview",
            usageCount = 1
        )
    }
}