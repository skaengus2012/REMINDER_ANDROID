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

package com.nlab.reminder.core.component.schedulelist.ui.modal

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nlab.reminder.core.component.schedulelist.ScheduleDeletionConfirmation
import com.nlab.reminder.core.kotlin.toPositiveInt
import com.nlab.reminder.core.translation.StringIds

/**
 * @author Doohyun
 */
@Composable
fun SchedulesDeleteConfirmationHandler(
    confirmation: ScheduleDeletionConfirmation,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    when (confirmation) {
        is ScheduleDeletionConfirmation.Present -> {
            BasicSchedulesDeleteConfirmBottomSheet(
                bodyMessage = stringResource(StringIds.content_schedules_delete_notice_in_list),
                deletionCount = confirmation.targetIds.value.size.toPositiveInt(),
                onConfirm = onConfirm,
                onCancel = onCancel
            )
        }

        is ScheduleDeletionConfirmation.Absent -> {
            // do nothing
        }
    }
}