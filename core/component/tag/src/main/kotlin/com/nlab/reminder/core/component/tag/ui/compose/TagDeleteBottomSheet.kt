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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.ui.compose.throttleClick
import com.nlab.reminder.core.ui.compose.tooling.preview.Previews
import com.nlab.reminder.core.component.tag.ui.getUsageCountLabel
import com.nlab.reminder.core.designsystem.compose.component.PlaneatBottomSheet
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.kotlin.toNonNegativeLong
import com.nlab.reminder.core.translation.PluralsIds
import com.nlab.reminder.core.translation.StringIds
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
@Composable
internal fun TagDeleteBottomSheet(
    tagNames: List<NonBlankString>,
    usageCount: NonNegativeLong,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    PlaneatBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState
    ) {
        TagDeleteBottomSheetContent(
            tagNames = tagNames,
            usageCount = usageCount,
            onConfirm = {
                coroutineScope.launch {
                    sheetState.hide()
                    onConfirm()
                }
            },
            onCancel = {
                coroutineScope.launch {
                    sheetState.hide()
                    onCancel()
                }
            }
        )
    }
}

@Composable
private fun TagDeleteBottomSheetContent(
    tagNames: List<NonBlankString>,
    usageCount: NonNegativeLong,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val tagNameSize = tagNames.size
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = pluralStringResource(id = PluralsIds.tag_delete, count = tagNameSize, tagNameSize),
            style = PlaneatTheme.typography.bodySmall,
            color = PlaneatTheme.colors.content1,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, top = 6.dp, end = 30.dp, bottom = 22.dp),
            style = PlaneatTheme.typography.bodySmall,
            text = when (tagNameSize) {
                0 -> ""
                1 -> {
                    val firstName = tagNames.first().value
                    getUsageCountLabel(
                        usageCount = usageCount,
                        transform = { count ->
                            pluralStringResource(
                                id = PluralsIds.tag_delete_dialog_description_size_1,
                                count = count,
                                firstName,
                                count
                            )
                        },
                        transformWhenOverflow = { count ->
                            pluralStringResource(
                                id = PluralsIds.tag_delete_dialog_description_size_1_overflow,
                                count = count,
                                firstName,
                                count
                            )
                        }
                    )
                }
                2 -> {
                    val firstName = tagNames.first().value
                    val secondName = tagNames.last().value
                    getUsageCountLabel(
                        usageCount = usageCount,
                        transform = { count ->
                            pluralStringResource(
                                id = PluralsIds.tag_delete_dialog_description_size_2,
                                count = count,
                                firstName,
                                secondName,
                                count
                            )
                        },
                        transformWhenOverflow = { count ->
                            pluralStringResource(
                                id = PluralsIds.tag_delete_dialog_description_size_2_overflow,
                                count = count,
                                firstName,
                                secondName,
                                count
                            )
                        }
                    )
                }
                else -> {
                    val firstName = tagNames.first().value
                    val extraSize = tagNameSize - 1
                    getUsageCountLabel(
                        usageCount = usageCount,
                        transform = { count ->
                            pluralStringResource(
                                id = PluralsIds.tag_delete_dialog_description_size_other,
                                count = count,
                                firstName,
                                extraSize,
                                count
                            )
                        },
                        transformWhenOverflow = { count ->
                            pluralStringResource(
                                id = PluralsIds.tag_delete_dialog_description_size_other_overflow,
                                count = count,
                                firstName,
                                extraSize,
                                count
                            )
                        }
                    )
                }
            },
            color = PlaneatTheme.colors.content1,
            textAlign = TextAlign.Center
        )
        HorizontalDivider(
            thickness = 0.5.dp,
            color = PlaneatTheme.colors.bgLine1,
        )
        TagEditDeleteButton(
            text = stringResource(id = StringIds.delete),
            fontColor = PlaneatTheme.colors.red1,
            onClick = onConfirm
        )
        TagEditDeleteButton(
            text = stringResource(id = StringIds.cancel),
            fontColor = PlaneatTheme.colors.content2,
            onClick = onCancel,
            modifier = Modifier.padding(bottom = 10.dp)
        )
    }
}

@Composable
private fun TagEditDeleteButton(
    text: String,
    fontColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = throttleClick(onClick = onClick),
                onClickLabel = text,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = PlaneatTheme.colors.bgRipple1),
                role = Role.Button
            ),
        color = Color.Transparent
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            text = text,
            textAlign = TextAlign.Center,
            color = fontColor,
            style = PlaneatTheme.typography.bodyLarge,
        )
    }
}

@Previews
@Composable
private fun TagEditDeleteBottomSheetContentPreview() {
    PlaneatTheme {
        TagDeleteBottomSheetContent(
            modifier = Modifier.background(PlaneatTheme.colors.bgDialogSurface),
            tagNames = listOf("Hello, TagDeleteBottomSheet".toNonBlankString()),
            usageCount = 5L.toNonNegativeLong(),
            onConfirm = {},
            onCancel = {},
        )
    }
}

@Previews
@Composable
private fun TwoSizeTagEditDeleteBottomSheetContentPreview() {
    PlaneatTheme {
        TagDeleteBottomSheetContent(
            modifier = Modifier.background(PlaneatTheme.colors.bgDialogSurface),
            tagNames = listOf(
                "one".toNonBlankString(),
                "two".toNonBlankString()
            ),
            usageCount = 5L.toNonNegativeLong(),
            onConfirm = {},
            onCancel = {},
        )
    }
}

@Previews
@Composable
private fun OtherSizeTagEditDeleteBottomSheetContentPreview() {
    PlaneatTheme {
        TagDeleteBottomSheetContent(
            modifier = Modifier.background(PlaneatTheme.colors.bgDialogSurface),
            tagNames = listOf(
                "one".toNonBlankString(),
                "two".toNonBlankString(),
                "three".toNonBlankString(),
                "four".toNonBlankString(),
                "five".toNonBlankString()
            ),
            usageCount = 5L.toNonNegativeLong(),
            onConfirm = {},
            onCancel = {},
        )
    }
}
