package com.nlab.reminder.core.component.schedulelist

import androidx.compose.runtime.Immutable
import com.nlab.reminder.core.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.kotlin.PositiveInt

/**
 * @author Doohyun
 */
@Immutable
sealed class CompletedSchedulesCleanupConfirmation {
    @ExcludeFromGeneratedTestReport
    data object Absent : CompletedSchedulesCleanupConfirmation()

    @ExcludeFromGeneratedTestReport
    data class Present(val completedCount: PositiveInt) : CompletedSchedulesCleanupConfirmation()
}