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

package com.nlab.reminder.core.data.repository.fake

import com.nlab.reminder.core.annotation.test.ExcludeFromGeneratedTestReport
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * TODO : Remove this class and Jacoco fake whitelist.
 * @author thalys
 */
@ExcludeFromGeneratedTestReport
@Deprecated("Fake method used")
internal object FakeScheduleRepositoryDelegate {
    fun getTodaySchedulesCount(): Flow<Long> = flow {
        delay((1_500L..3000L).random())
        emit((0L..100).random())
    }

    fun getTimetableSchedulesCount(): Flow<Long> = flowOf((0L..100).random())

    fun getAllSchedulesCount(): Flow<Long> = flowOf((0L..100).random())
}