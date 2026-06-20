package com.nlab.reminder.core.component.schedulelist

import com.nlab.reminder.core.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.kotlin.NonNegativeInt

/**
 * @author Doohyun
 */
@ExcludeFromGeneratedTestReport
data class ScheduleListStats(
    val completedCount: NonNegativeInt,
    val selectedCount: NonNegativeInt
)