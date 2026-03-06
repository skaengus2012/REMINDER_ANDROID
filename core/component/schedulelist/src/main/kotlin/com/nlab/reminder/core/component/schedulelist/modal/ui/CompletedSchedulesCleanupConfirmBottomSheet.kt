/*
 * Copyright (C) 2026 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.schedulelist.modal.ui

import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.nlab.reminder.core.androidx.compose.ui.throttleClick
import com.nlab.reminder.core.designsystem.compose.component.PlaneatBottomButton
import com.nlab.reminder.core.designsystem.compose.component.PlaneatBottomSheet
import com.nlab.reminder.core.designsystem.compose.component.PlaneatBottomSheetBody
import com.nlab.reminder.core.designsystem.compose.component.PlaneatBottomSheetTitle
import com.nlab.reminder.core.kotlin.NonNegativeInt
import com.nlab.reminder.core.translation.PluralsIds
import com.nlab.reminder.core.translation.StringIds
import kotlinx.coroutines.launch

/**
 * @author Thalys
 */
@Composable
fun CompletedSchedulesCleanupConfirmBottomSheet(
    completedSchedulesCount: NonNegativeInt,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    PlaneatBottomSheet(
        title = PlaneatBottomSheetTitle.None,
        body = PlaneatBottomSheetBody.Text(
            pluralStringResource(
                id = PluralsIds.content_completed_schedules_cleanup_notice_in_list,
                count = completedSchedulesCount.value,
                completedSchedulesCount.value
            )
        ),
        button = PlaneatBottomButton.TwoButton(
            primaryButtonText = pluralStringResource(
                id = PluralsIds.confirm_delete_reminder,
                count = completedSchedulesCount.value,
                completedSchedulesCount.value
            ),
            onPrimaryButtonClicked = throttleClick {
                coroutineScope.launch {
                    sheetState.hide()
                    onConfirm()
                }
            },
            secondaryButtonText = stringResource(StringIds.cancel),
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