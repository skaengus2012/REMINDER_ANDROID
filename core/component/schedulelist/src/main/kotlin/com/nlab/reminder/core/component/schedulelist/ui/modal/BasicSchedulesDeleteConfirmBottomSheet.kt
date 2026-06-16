package com.nlab.reminder.core.component.schedulelist.ui.modal

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
internal fun BasicSchedulesDeleteConfirmBottomSheet(
    bodyMessage: String,
    deletionCount: NonNegativeInt,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    PlaneatBottomSheet(
        title = PlaneatBottomSheetTitle.None,
        body = PlaneatBottomSheetBody.Text(bodyMessage),
        button = PlaneatBottomButton.TwoButton(
            primaryButtonText = pluralStringResource(
                id = PluralsIds.confirm_delete_reminder,
                count = deletionCount.value,
                deletionCount.value
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