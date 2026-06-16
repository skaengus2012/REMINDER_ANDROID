package com.nlab.reminder.core.component.schedulelist.ui.modal

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nlab.reminder.core.kotlin.NonNegativeInt
import com.nlab.reminder.core.translation.StringIds

/**
 * @author Doohyun
 */
@Composable
fun SchedulesDeleteConfirmBottomSheet(
    deletionCount: NonNegativeInt,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    BasicSchedulesDeleteConfirmBottomSheet(
        bodyMessage = stringResource(StringIds.content_schedules_delete_notice_in_list),
        deletionCount = deletionCount,
        onConfirm = onConfirm,
        onCancel = onCancel
    )
}