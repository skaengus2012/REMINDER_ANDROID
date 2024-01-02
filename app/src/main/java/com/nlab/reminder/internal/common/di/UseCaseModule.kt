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

package com.nlab.reminder.internal.common.di

import com.nlab.reminder.core.data.repository.ScheduleCompleteMarkRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.domain.CompleteScheduleWithMarkUseCase
import com.nlab.reminder.core.kotlin.coroutine.Delay
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

/**
 * @author thalys
 */
@Module
@InstallIn(ViewModelComponent::class)
internal abstract class UseCaseModule {
    companion object {
        @Reusable
        @Provides
        fun provideCompleteScheduleWithMarkUseCase(
            scheduleRepository: ScheduleRepository,
            scheduleCompleteMarkRepository: ScheduleCompleteMarkRepository
        ) = CompleteScheduleWithMarkUseCase(
            scheduleRepository,
            scheduleCompleteMarkRepository,
            aggregateDelay = Delay(timeMillis = 500)
        )
    }
}