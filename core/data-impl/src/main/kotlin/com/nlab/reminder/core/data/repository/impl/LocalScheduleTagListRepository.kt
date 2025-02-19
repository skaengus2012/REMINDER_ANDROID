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

package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.data.repository.ScheduleTagListRepository
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.catching
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.toNonNegativeLong
import com.nlab.reminder.core.kotlinx.coroutine.flow.map
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * @author Thalys
 */
class LocalScheduleTagListRepository(
    private val scheduleTagListDAO: ScheduleTagListDAO,
) : ScheduleTagListRepository {
    override suspend fun getTagUsageCount(tagId: TagId): Result<NonNegativeLong> = catching {
        scheduleTagListDAO
            .findTagUsageCount(tagId = tagId.rawId)
            .toNonNegativeLong()
    }

    override fun getScheduleTagListAsStream(scheduleIds: Set<ScheduleId>): Flow<Map<ScheduleId, Set<TagId>>> =
        scheduleTagListDAO
            .findByScheduleIdsAsStream(scheduleIds.toSet(ScheduleId::rawId))
            .distinctUntilChanged()
            .map { entities ->
                entities
                    .groupBy(keySelector = { ScheduleId(it.scheduleId) }, valueTransform = { TagId(it.tagId) })
                    .mapValues { (_, value) -> value.toSet() }
            }
}