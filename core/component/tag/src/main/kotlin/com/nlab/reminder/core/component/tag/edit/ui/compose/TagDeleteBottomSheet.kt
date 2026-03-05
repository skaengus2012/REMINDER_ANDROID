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

import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.nlab.reminder.core.androidx.compose.ui.throttleClick
import com.nlab.reminder.core.component.displayformat.ui.tagDisplayText
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.designsystem.compose.component.PlaneatBottomSheet
import com.nlab.reminder.core.designsystem.compose.component.PlaneatBottomButton
import com.nlab.reminder.core.designsystem.compose.component.PlaneatBottomSheetBody
import com.nlab.reminder.core.designsystem.compose.component.PlaneatBottomSheetTitle
import com.nlab.reminder.core.kotlin.NonNegativeInt
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
    val tagSize = tags.size
    PlaneatBottomSheet(
        title = PlaneatBottomSheetTitle.Text(
            text = pluralStringResource(
                id = PluralsIds.title_tag_delete_dialog,
                count = tagSize,
                tagSize
            )
        ),
        body = PlaneatBottomSheetBody.Text(
            text = when (tagSize) {
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
                    val extraSize = tagSize - 1
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
            }
        ),
        button = PlaneatBottomButton.TwoButton(
            primaryButtonText = stringResource(id = StringIds.label_delete),
            onPrimaryButtonClicked = throttleClick {
                coroutineScope.launch {
                    sheetState.hide()
                    onConfirm()
                }
            },
            secondaryButtonText = stringResource(id = StringIds.cancel),
            onSecondaryButtonClicked = throttleClick {
                coroutineScope.launch {
                    sheetState.hide()
                    onCancel()
                }
            }
        ),
        onDismissRequest = onCancel,
        sheetState = sheetState
    )
}