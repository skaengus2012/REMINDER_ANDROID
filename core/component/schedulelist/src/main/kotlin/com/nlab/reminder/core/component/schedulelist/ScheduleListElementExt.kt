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

package com.nlab.reminder.core.component.schedulelist

import com.nlab.reminder.core.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.kotlin.toNonNegativeInt

/**
 * @author Doohyun
 */
fun ScheduleListElement.userScheduleListResourceOrNull(): UserScheduleListResource? =
    this as? UserScheduleListResource

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

@ExcludeFromGeneratedTestReport
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

@ExcludeFromGeneratedTestReport
inline fun List<ScheduleListElement>.mapToScheduleIdsList(
    predicate: (UserScheduleListResource) -> Boolean
): List<ScheduleId> = mapNotNull { element ->
    element.userScheduleListResourceOrNull()
        ?.takeIf(predicate)
        ?.schedule
        ?.id
}