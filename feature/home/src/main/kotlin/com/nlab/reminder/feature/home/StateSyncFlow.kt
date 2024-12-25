/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.feature.home

import com.nlab.reminder.core.data.repository.GetScheduleCountQuery
import com.nlab.reminder.core.data.repository.GetTagQuery
import com.nlab.reminder.core.kotlinx.coroutine.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Currently, no errors occur because the local DB is used.
 * Later, when the flow prevents errors, additional actions need to be added.
 *
 * @author Doohyun
 * @return Flow emits [HomeAction.StateSynced].
 */
@Suppress("FunctionName")
internal fun StateSyncFlow(environment: HomeEnvironment): Flow<HomeAction> = with(environment) {
    combine(
        scheduleRepository
            .getScheduleCountAsStream(GetScheduleCountQuery.Today),
        scheduleRepository
            .getScheduleCountAsStream(GetScheduleCountQuery.Timetable),
        scheduleRepository
            .getScheduleCountAsStream(GetScheduleCountQuery.All),
        tagRepository
            .getTagsAsStream(GetTagQuery.All)
            .map { tags -> tags.sortedBy { it.name.value } },
        transform = HomeAction::StateSynced
    )
}