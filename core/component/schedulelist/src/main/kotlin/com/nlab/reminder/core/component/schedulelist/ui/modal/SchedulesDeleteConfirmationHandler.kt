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
            // do noting
        }
    }
}