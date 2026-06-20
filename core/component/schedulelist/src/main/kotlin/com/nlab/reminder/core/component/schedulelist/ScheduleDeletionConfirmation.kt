package com.nlab.reminder.core.component.schedulelist

import androidx.compose.runtime.Immutable
import com.nlab.reminder.core.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.kotlin.collections.NonEmptySet

/**
 * @author Doohyun
 */
@Immutable
sealed class ScheduleDeletionConfirmation {
    @ExcludeFromGeneratedTestReport
    data object Absent : ScheduleDeletionConfirmation()

    @ExcludeFromGeneratedTestReport
    data class Present(val targetIds: NonEmptySet<ScheduleId>) : ScheduleDeletionConfirmation()
}