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

package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.data.model.ScheduleDeletionBacklog
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.repository.ScheduleDeletionBacklogRepository
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.local.database.dao.ScheduleDeletionBacklogDAO
import com.nlab.reminder.core.local.database.entity.ScheduleDeletionBacklogEntity

/**
 * @author Doohyun
 */
class LocalScheduleDeletionBacklogRepository(
    private val scheduleDeletionBacklogDAO: ScheduleDeletionBacklogDAO
) : ScheduleDeletionBacklogRepository {
    override suspend fun save(scheduleIds: Set<ScheduleId>): Result<Unit> = runCatching {
        scheduleDeletionBacklogDAO.insertAll(scheduleIds.toSet { it.rawId })
    }

    override suspend fun getBacklogs(): Result<Set<ScheduleDeletionBacklog>> = runCatching {
        scheduleDeletionBacklogDAO.getAll().toBacklogs()
    }
}

private fun List<ScheduleDeletionBacklogEntity>.toBacklogs(): Set<ScheduleDeletionBacklog> =
    toSet(::ScheduleDeletionBacklog)
