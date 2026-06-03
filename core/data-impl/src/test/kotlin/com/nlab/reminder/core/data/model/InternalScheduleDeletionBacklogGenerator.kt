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

package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.kotlin.PositiveInt
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.toPositiveInt
import com.nlab.reminder.core.local.database.entity.ScheduleDeletionBacklogEntity
import com.nlab.testkit.faker.genIntGreaterThanZero

typealias ScheduleDeletionBacklogAndEntity = Pair<ScheduleDeletionBacklog, ScheduleDeletionBacklogEntity>

/**
 * @author Doohyun
 */
fun genScheduleDeletionBacklogAndEntity(
    backlog: ScheduleDeletionBacklog = genScheduleDeletionBacklog()
): ScheduleDeletionBacklogAndEntity {
    return backlog to ScheduleDeletionBacklogEntity(
        backlogId = backlog.id.value,
        scheduleId = backlog.scheduleId.rawId
    )
}

fun genScheduleDeletionBacklogAndEntities(
    count: PositiveInt = genIntGreaterThanZero(max = 10).toPositiveInt()
): Set<ScheduleDeletionBacklogAndEntity> {
    return (1..count.value).toSet { index ->
        genScheduleDeletionBacklogAndEntity(
            backlog = genScheduleDeletionBacklog(id = ScheduleDeletionBacklogId(index.toLong()))
        )
    }
}
