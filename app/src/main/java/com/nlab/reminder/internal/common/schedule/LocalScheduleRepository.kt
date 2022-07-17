/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.internal.common.schedule

import com.nlab.reminder.core.kotlin.flow.map
import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.domain.common.schedule.ScheduleItemRequestConfig
import com.nlab.reminder.domain.common.schedule.ScheduleRepository
import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.util.DatabaseQualifier
import com.nlab.reminder.internal.common.android.database.ScheduleDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * @author Doohyun
 */
class LocalScheduleRepository @Inject constructor(
    private val scheduleDao: ScheduleDao,
    @DatabaseQualifier private val dispatcher: CoroutineDispatcher
) : ScheduleRepository {
    override fun get(requestConfig: ScheduleItemRequestConfig): Flow<List<Schedule>> =
        scheduleDao.find(isComplete = requestConfig.isComplete)
            .flowOn(dispatcher)
            .map { scheduleWithTags ->
                scheduleWithTags.map { (scheduleEntity, tagEntities) ->
                    Schedule(
                        scheduleId = scheduleEntity.scheduleId,
                        title = scheduleEntity.title,
                        note = scheduleEntity.description ?: "",
                        url = scheduleEntity.url ?: "",
                        tags = tagEntities.map { Tag(it.tagId, it.name) }.sortedBy { it.name },
                        visiblePriority = scheduleEntity.visiblePriority,
                        isComplete = scheduleEntity.isComplete
                    )
                }
            }
}