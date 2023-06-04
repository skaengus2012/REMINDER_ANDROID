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

import com.nlab.reminder.domain.common.data.repository.ScheduleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * @author Doohyun
 */
@Deprecated(message = "FakeScheduleRepository was used")
internal class FakeScheduleRepository @Inject constructor() : ScheduleRepository {
    override fun getTodaySchedulesCount(): Flow<Long> = flow {
        delay((1_500L..3000L).random())
        emit((0L..100).random())
    }

    override fun getTimetableSchedulesCount(): Flow<Long> = flow {
        emit((0L..100).random())
    }

    override fun getAllSchedulesCount(): Flow<Long> = flow {
        emit((0L..100).random())
    }
}