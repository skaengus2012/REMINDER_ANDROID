/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.kotlin.PositiveInt
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.toPositiveInt
import com.nlab.reminder.core.local.database.entity.ScheduleCompletionBacklogEntity
import com.nlab.testkit.faker.genIntGreaterThanZero

typealias ScheduleCompletionBacklogAndEntity = Pair<ScheduleCompletionBacklog, ScheduleCompletionBacklogEntity>

/**
 * @author Doohyun
 */
fun genScheduleCompletionBacklogAndEntity(
    backlog: ScheduleCompletionBacklog = genScheduleCompletionBacklog()
): ScheduleCompletionBacklogAndEntity {
    return backlog to ScheduleCompletionBacklogEntity(
        backlogId = backlog.id.rawId,
        scheduleId = backlog.scheduleId.rawId,
        targetCompleted = backlog.targetCompleted,
        insertOrder = backlog.priority.value
    )
}

fun genScheduleCompletionBacklogAndEntities(
    count: PositiveInt = genIntGreaterThanZero(max = 10).toPositiveInt()
): Set<ScheduleCompletionBacklogAndEntity> {
    return (1..count.value).toSet { index ->
        genScheduleCompletionBacklogAndEntity(
            backlog = genScheduleCompletionBacklog(id = ScheduleCompletionBacklogId(index.toLong())
            )
        )
    }
}