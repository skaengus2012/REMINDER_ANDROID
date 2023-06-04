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

import com.nlab.reminder.core.util.test.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.domain.common.data.model.Tag
import com.nlab.statekit.State
import kotlinx.collections.immutable.ImmutableList

/**
 * @author Doohyun
 */
sealed interface HomeUiState : State {
    object Loading : HomeUiState

    @ExcludeFromGeneratedTestReport
    data class Success(
        val todayScheduleCount: Long,
        val timetableScheduleCount: Long,
        val allScheduleCount: Long,
        val tags: ImmutableList<Tag>,
        val todayScheduleShown: Boolean = false,
        val timetableScheduleShown: Boolean = false,
        val allScheduleShown: Boolean = false,
    ) : HomeUiState
}

internal fun HomeUiState.Success.withPageShown(
    todayScheduleShown: Boolean = false,
    timetableScheduleShown: Boolean = false,
    allScheduleShown: Boolean = false,
) = copy(
    todayScheduleShown = todayScheduleShown,
    timetableScheduleShown = timetableScheduleShown,
    allScheduleShown = allScheduleShown,
)