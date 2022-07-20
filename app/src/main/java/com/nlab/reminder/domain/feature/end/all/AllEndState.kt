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

package com.nlab.reminder.domain.feature.end.all

import androidx.paging.PagingData
import com.nlab.reminder.core.state.State
import com.nlab.reminder.core.util.annotation.test.Generated
import com.nlab.reminder.domain.common.schedule.Schedule

/**
 * @author Doohyun
 */
sealed class AllEndState private constructor() : State {
    object Init : AllEndState()
    object Loading : AllEndState()

    @Generated
    data class Loaded(
        val doingSchedules: List<Schedule>,
        val doneSchedules: PagingData<Schedule>,
        val isDoneScheduleShown: Boolean
    ) : AllEndState()
}