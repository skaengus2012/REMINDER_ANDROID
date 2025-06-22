/*
 * Copyright (C) 2025 The N's lab Open Source Project
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
import com.nlab.reminder.core.androidx.compose.ui.throttleClick
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.component.displayformat.ui.tagDisplayText
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.designsystem.compose.component.PlaneatBottomSheet
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme
import com.nlab.reminder.core.kotlin.NonNegativeInt
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.kotlin.toNonNegativeInt
import com.nlab.reminder.core.translation.PluralsIds
import com.nlab.reminder.core.translation.StringIds
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
@Composable
internal fun TagDeleteBottomSheet(
    tags: List<Tag>,
    usageCount: NonNegativeInt,
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
            tags = tags,
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
    tags: List<Tag>,
    usageCount: NonNegativeInt,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val tagNameSize = tags.size
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = pluralStringResource(id = PluralsIds.title_tag_delete_dialog, count = tagNameSize, tagNameSize),
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
                    val firstTagDisplayText = tagDisplayText(tags.first())
                    getUsageCountLabel(
                        usageCount = usageCount,
                        transform = { count ->
                            pluralStringResource(
                                id = PluralsIds.content_tag_delete_dialog_single,
                                count = count,
                                firstTagDisplayText,
                                count
                            )
                        },
                        transformWhenOverflow = { count ->
                            pluralStringResource(
                                id = PluralsIds.content_tag_delete_dialog_single_overflow,
                                count = count,
                                firstTagDisplayText,
                                count
                            )
                        }
                    )
                }
                2 -> {
                    val firstTagDisplayText = tagDisplayText(tags.first())
                    val lastTagDisplayText = tagDisplayText(tags.last())
                    getUsageCountLabel(
                        usageCount = usageCount,
                        transform = { count ->
                            pluralStringResource(
                                id = PluralsIds.content_tag_delete_dialog_double,
                                count = count,
                                firstTagDisplayText,
                                lastTagDisplayText,
                                count
                            )
                        },
                        transformWhenOverflow = { count ->
                            pluralStringResource(
                                id = PluralsIds.content_tag_delete_dialog_double_overflow,
                                count = count,
                                firstTagDisplayText,
                                lastTagDisplayText,
                                count
                            )
                        }
                    )
                }
                else -> {
                    val firstTagDisplayText = tagDisplayText(tags.first())
                    val extraSize = tagNameSize - 1
                    getUsageCountLabel(
                        usageCount = usageCount,
                        transform = { count ->
                            pluralStringResource(
                                id = PluralsIds.content_tag_delete_dialog_other,
                                count = count,
                                firstTagDisplayText,
                                extraSize,
                                count
                            )
                        },
                        transformWhenOverflow = { count ->
                            pluralStringResource(
                                id = PluralsIds.content_tag_delete_dialog_other_overflow,
                                count = count,
                                firstTagDisplayText,
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
            tags = listOf(
                Tag(
                    id = TagId(rawId = 1),
                    name = "SingleTag".toNonBlankString()
                )
            ),
            usageCount = 5.toNonNegativeInt(),
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
            tags = listOf(
                Tag(
                    id = TagId(rawId = 1),
                    name = "one".toNonBlankString()
                ),
                Tag(
                    id = TagId(rawId = 2),
                    name = "two".toNonBlankString()
                )
            ),
            usageCount = 5.toNonNegativeInt(),
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
            tags = listOf(
                Tag(
                    id = TagId(rawId = 1),
                    name = "one".toNonBlankString()
                ),
                Tag(
                    id = TagId(rawId = 2),
                    name = "two".toNonBlankString()
                ),
                Tag(
                    id = TagId(rawId = 3),
                    name = "three".toNonBlankString()
                ),
                Tag(
                    id = TagId(rawId = 4),
                    name = "four".toNonBlankString()
                ),
                Tag(
                    id = TagId(rawId = 5),
                    name = "five".toNonBlankString()
                ),
            ),
            usageCount = 5.toNonNegativeInt(),
            onConfirm = {},
            onCancel = {},
        )
    }
}
