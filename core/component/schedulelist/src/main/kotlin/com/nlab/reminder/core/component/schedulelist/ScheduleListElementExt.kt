package com.nlab.reminder.core.component.schedulelist

import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.kotlin.toNonNegativeInt

/**
 * @author Doohyun
 */
fun ScheduleListElement.userScheduleListResourceOrNull(): UserScheduleListResource? {
    return this as? UserScheduleListResource
}

fun Collection<ScheduleListElement>.toScheduleListStats(
    needCompletedCount: Boolean,
    needSelectedCount: Boolean
): ScheduleListStats {
    var completedCount = 0
    var selectedCount = 0
    for (element in this) {
        val resource = element.userScheduleListResourceOrNull() ?: continue
        if (needCompletedCount && resource.schedule.isComplete) ++completedCount
        if (needSelectedCount && resource.selected) ++selectedCount
    }

    return ScheduleListStats(
        completedCount = completedCount.toNonNegativeInt(),
        selectedCount = selectedCount.toNonNegativeInt()
    )
}

inline fun Collection<ScheduleListElement>.mapToScheduleIds(
    predicate: (UserScheduleListResource) -> Boolean
): Set<ScheduleId> = buildSet {
    this@mapToScheduleIds.forEach { element ->
        element.userScheduleListResourceOrNull()
            ?.takeIf(predicate)
            ?.schedule
            ?.id
            ?.run(::add)
    }
}

inline fun List<ScheduleListElement>.mapToScheduleIdsList(
    predicate: (UserScheduleListResource) -> Boolean
): List<ScheduleId> = mapNotNull { element ->
    element.userScheduleListResourceOrNull()
        ?.takeIf(predicate)
        ?.schedule
        ?.id
}