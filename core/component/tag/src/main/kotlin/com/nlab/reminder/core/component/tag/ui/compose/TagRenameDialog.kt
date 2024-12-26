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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.androidx.compose.ui.rememberDebouncedTextFieldValueState
import com.nlab.reminder.core.androidx.compose.ui.throttleClick
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.component.tag.ui.getUsageCountLabel
import com.nlab.reminder.core.designsystem.compose.component.PlaneatDialog
import com.nlab.reminder.core.designsystem.compose.theme.DrawableIds
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.kotlin.toNonNegativeLong
import com.nlab.reminder.core.translation.PluralsIds
import com.nlab.reminder.core.translation.StringIds
import kotlinx.coroutines.delay

/**
 * @author Doohyun
 */
@Composable
internal fun TagRenameDialog(
    value: String,
    tagName: NonBlankString,
    usageCount: NonNegativeLong,
    shouldKeyboardShown: Boolean,
    onTextChanged: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    PlaneatDialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier.width(250.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TagRenameTitle(
                modifier = Modifier.padding(start = 15.dp, top = 15.dp, end = 15.dp)
            )
            TagRenameDescription(
                modifier = Modifier.padding(start = 35.dp, top = 2.5.dp, end = 35.dp, bottom = 15.dp),
                tagName = tagName,
                usageCount = usageCount
            )
            TagRenameInputField(
                modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 10.dp),
                value = value,
                shouldKeyboardShown = shouldKeyboardShown,
                onTextChanged = onTextChanged
            )
            TagDialogButtons(
                onCancel = onCancel,
                onConfirm = onConfirm
            )
        }
    }
}

@Composable
private fun TagRenameTitle(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = stringResource(StringIds.tag_rename),
        style = PlaneatTheme.typography
            .bodyLarge
            .copy(fontWeight = FontWeight.Bold),
        color = PlaneatTheme.colors.content1
    )
}

@Composable
private fun TagRenameDescription(
    tagName: NonBlankString,
    usageCount: NonNegativeLong,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = getUsageCountLabel(
            usageCount = usageCount,
            transform = { count ->
                pluralStringResource(PluralsIds.tag_rename_dialog_description, count, tagName.value, count)
            },
            transformWhenOverflow = { count ->
                stringResource(StringIds.tag_rename_dialog_description_overflow, tagName.value, count)
            }
        ),
        style = PlaneatTheme.typography.bodySmall,
        color = PlaneatTheme.colors.content1,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun TagRenameInputField(
    value: String,
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
            .border(
                width = 0.5.dp,
                color = PlaneatTheme.colors.bgLine1,
                shape = RoundedCornerShape(5.dp)
            )
            .background(PlaneatTheme.colors.bg2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var debouncedTextFieldValue by rememberDebouncedTextFieldValueState(
            value = value,
            onTextChanged = onTextChanged
        )
        val isClearBtnVisible: Boolean by remember {
            derivedStateOf { debouncedTextFieldValue.text.isNotEmpty() }
        }
        TagRenameTextField(
            modifier = Modifier
                .padding(start = 9.dp)
                .weight(1f)
                .heightIn(min = 30.dp)
                .focusRequester(focusRequester),
            value = debouncedTextFieldValue,
            onValueChange = { debouncedTextFieldValue = it },
        )
        TagRenameClearButton(
            isVisible = isClearBtnVisible,
            onClick = {
                debouncedTextFieldValue = debouncedTextFieldValue.copy("", selection = TextRange.Zero)
            }
        )
    }
}

@Composable
private fun TagRenameTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        textStyle = PlaneatTheme.typography
            .bodyMedium
            .copy(
                textAlign = TextAlign.Start,
                color = PlaneatTheme.colors.content1
            ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                innerTextField()
            }
        },
        singleLine = true,
        cursorBrush = SolidColor(PlaneatTheme.colors.point1),
    )
}

@Composable
private fun TagRenameClearButton(
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .alpha(if (isVisible) 1f else 0f)
            .clickable(
                enabled = isVisible,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = throttleClick(onClick = onClick),
                role = Role.Button
            ),
        color = Color.Transparent
    ) {
        Image(
            modifier = Modifier
                .size(25.dp)
                .padding(6.dp),
            painter = painterResource(id = DrawableIds.ic_cancel_popup),
            contentDescription = null
        )
    }
}

@Previews
@Composable
private fun TagRenameDialogPreview() {
    PlaneatTheme {
        Box(modifier = Modifier.size(300.dp)) {
            TagRenameDialog(
                value = "Hello, TagRenameDialog!",
                tagName = "Tag".toNonBlankString(),
                usageCount = 5L.toNonNegativeLong(),
                shouldKeyboardShown = false,
                onTextChanged = {},
                onCancel = {},
                onConfirm = {}
            )
        }
    }
}
