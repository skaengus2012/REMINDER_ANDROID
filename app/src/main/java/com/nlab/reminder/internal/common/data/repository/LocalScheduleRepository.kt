/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.internal.common.data.repository

import com.nlab.reminder.core.kotlin.coroutine.flow.map
import com.nlab.reminder.core.util.test.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.domain.common.data.model.Schedule
import com.nlab.reminder.domain.common.data.repository.ScheduleGetStreamRequest
import com.nlab.reminder.domain.common.data.repository.ScheduleRepository
import com.nlab.reminder.internal.common.android.database.ScheduleDao
import com.nlab.reminder.internal.common.data.model.toModels
import com.nlab.reminder.internal.common.data.repository.fake.FakeScheduleRepositoryDelegate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @author Doohyun
 */
internal class LocalScheduleRepository @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {
    @ExcludeFromGeneratedTestReport
    override fun getTodaySchedulesCount(): Flow<Long> =
        FakeScheduleRepositoryDelegate.getTodaySchedulesCount()

    @ExcludeFromGeneratedTestReport
    override fun getTimetableSchedulesCount(): Flow<Long> =
        FakeScheduleRepositoryDelegate.getTimetableSchedulesCount()

    @ExcludeFromGeneratedTestReport
    override fun getAllSchedulesCount(): Flow<Long> =
        FakeScheduleRepositoryDelegate.getAllSchedulesCount()

    override fun getAsStream(request: ScheduleGetStreamRequest): Flow<List<Schedule>> {
        val entitiesFlow = when (request) {
            is ScheduleGetStreamRequest.All -> scheduleDao.findAsStream()
            is ScheduleGetStreamRequest.ByComplete -> scheduleDao.findByCompleteAsStream(request.isComplete)
        }

        return entitiesFlow.map { it.toModels() }
    }
}