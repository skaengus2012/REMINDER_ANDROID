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
 * @author Doohyun
 */
@Composable
fun SelectedSchedulesDeleteConfirmBottomSheet(
    selectedScheduleCount: NonNegativeInt,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    PlaneatBottomSheet(
        title = PlaneatBottomSheetTitle.None,
        body = PlaneatBottomSheetBody.None,
        button = PlaneatBottomButton.TwoButton(
            primaryButtonText = pluralStringResource(
                id = PluralsIds.confirm_delete_reminder,
                count = selectedScheduleCount.value,
                selectedScheduleCount.value
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