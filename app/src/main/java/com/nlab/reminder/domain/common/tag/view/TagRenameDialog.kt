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

package com.nlab.reminder.domain.common.tag.view

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nlab.reminder.R
import com.nlab.reminder.domain.common.android.designsystem.component.PointColorPressButton
import com.nlab.reminder.domain.common.android.designsystem.component.ReminderThemeDialog
import com.nlab.reminder.domain.common.android.designsystem.theme.ReminderTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce

/**
 * @author Doohyun
 */
@Composable
fun TagRenameDialog(
    initText: String,
    tagName: String,
    usageCount: Int,
    shouldKeyboardShown: Boolean = false,
    onTextChanged: (String) -> Unit = {},
    onCancel: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {
    ReminderThemeDialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier
                .width(250.dp)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(top = 15.dp),
                    text = LocalContext.current.getString(R.string.tag_rename),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ReminderTheme.colors.font1
                )

                Text(
                    modifier = Modifier.padding(start = 20.dp, top = 2.5.dp, end = 20.dp, bottom = 15.dp),
                    text = LocalContext.current.resources.getQuantityString(
                        R.plurals.tag_rename_dialog_description,
                        usageCount,
                        tagName,
                        usageCount
                    ),
                    fontSize = 12.sp,
                    color = ReminderTheme.colors.font1,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Center
                )

                TagRenameTextBox(
                    initText = initText,
                    shouldKeyboardShown = shouldKeyboardShown,
                    onTextChanged = onTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                )
            }
            Divider(
                thickness = 0.5.dp,
                color = ReminderTheme.colors.bgLine2
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)

            ) {
                PointColorPressButton(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = onCancel
                ) { contentColor ->
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        color = contentColor,
                        text = LocalContext.current.getString(R.string.cancel),
                        fontSize = 16.5.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(
                    modifier = Modifier
                        .width(0.5.dp)
                        .fillMaxHeight()
                        .background(ReminderTheme.colors.bgLine2)
                )

                PointColorPressButton(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = onConfirm
                ) { contentColor ->
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        color = contentColor,
                        text = LocalContext.current.getString(R.string.ok),
                        fontSize = 16.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TagRenameTextBox(
    initText: String,
    shouldKeyboardShown: Boolean,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    if (shouldKeyboardShown) {
        val keyboardController = LocalSoftwareKeyboardController.current
        LaunchedEffect(focusRequester) {
            focusRequester.requestFocus()
            delay(100) // Make sure you have delay here
            keyboardController?.show()
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .border(width = 0.5.dp, color = ReminderTheme.colors.bgLine1, shape = RoundedCornerShape(5.dp))
            .background(ReminderTheme.colors.bg2), verticalAlignment = Alignment.CenterVertically
    ) {
        var localTextFieldValue by remember {
            mutableStateOf(TextFieldValue(initText, selection = TextRange(initText.length)))
        }
        val isClearBtnVisible: Boolean by remember {
            derivedStateOf { localTextFieldValue.text.isNotEmpty() }
        }

        LaunchedEffect(onTextChanged) {
            snapshotFlow { localTextFieldValue }
                .debounce(100L)
                .collect { onTextChanged(it.text) }
        }

        BasicTextField(
            modifier = Modifier
                .padding(start = 9.dp)
                .weight(1f)
                .wrapContentHeight()
                .defaultMinSize(minWidth = 0.dp, minHeight = 30.dp)
                .focusRequester(focusRequester),
            value = localTextFieldValue,
            onValueChange = { localTextFieldValue = it },
            textStyle = LocalTextStyle.current.copy(
                color = ReminderTheme.colors.font1,
                fontSize = 14.sp,
                textAlign = TextAlign.Start,
            ),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxWidth()) {
                    innerTextField()
                }
            },
            singleLine = true,
            cursorBrush = SolidColor(ReminderTheme.colors.pointColor1),
        )
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .padding(end = 3.dp, top = 0.7.dp)
                .height(25.dp)
                .clickable(
                    enabled = isClearBtnVisible,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { localTextFieldValue = localTextFieldValue.copy("", selection = TextRange.Zero) },
                    role = Role.Button
                )
                .alpha(if (isClearBtnVisible) 1f else 0f),
            color = Color.Transparent
        ) {
            Image(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(13.dp),
                painter = painterResource(id = R.drawable.ic_cancel_popup),
                contentDescription = null
            )
        }
    }
}

@Preview(
    name = "LightTagRenameDialogPreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkTagRenameDialogPreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun TagRenameDialogPreview() {
    ReminderTheme {
        TagRenameDialog(
            initText = "Modify Tag Name...",
            tagName = "Hello, TagRenameDialog check long text",
            usageCount = 2
        )
    }
}