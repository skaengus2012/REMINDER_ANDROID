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

package com.nlab.reminder.domain.feature.home

import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.coroutine.flow.combine
import com.nlab.statekit.middleware.epic.Epic
import com.nlab.statekit.util.buildDslEpic
import javax.inject.Inject

/**
 * @author thalys
 */
internal class HomeEpic @Inject constructor(
    tagRepository: TagRepository,
    scheduleRepository: ScheduleRepository
) : Epic<HomeAction> by buildDslEpic(buildDSL = {
    whileStateUsed {
        combine(
            scheduleRepository.getTodaySchedulesCount(),
            scheduleRepository.getTimetableSchedulesCount(),
            scheduleRepository.getAllSchedulesCount(),
            tagRepository.get(),
        ) { todaySchedulesCount, timetableSchedulesCount, allSchedulesCount, tags ->
            HomeAction.SummaryLoaded(
                todaySchedulesCount = todaySchedulesCount,
                timetableSchedulesCount = timetableSchedulesCount,
                allSchedulesCount = allSchedulesCount,
                tags = tags
            )
        }
    }
})