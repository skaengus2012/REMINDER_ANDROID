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

package com.nlab.reminder.core.component.schedule.di

import android.content.Context
import com.nlab.reminder.core.component.schedule.UpdateScheduleCompletionUseCase
import com.nlab.reminder.core.component.schedule.infra.RegisterScheduleCompleteJobUseCaseImpl
import com.nlab.reminder.core.data.model.ScheduleCompletionBacklog
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.repository.ScheduleCompletionBacklogRepository
import com.nlab.reminder.core.inject.qualifiers.coroutine.AppScope
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

/**
 * @author Thalys
 */
@Module
@InstallIn(SingletonComponent::class)
internal object AppScopeScheduleModule {
    @Reusable
    @Provides
    fun provideUpdateScheduleCompletionUseCase(
        @ApplicationContext context: Context,
        @AppScope coroutineScope: CoroutineScope,
        scheduleCompletionBacklogRepository: ScheduleCompletionBacklogRepository,
    ): UpdateScheduleCompletionUseCase = UpdateScheduleCompletionUseCase(
        coroutineScope = coroutineScope,
        scheduleCompletionBacklogRepository = object :
            ScheduleCompletionBacklogRepository by scheduleCompletionBacklogRepository {
            override suspend fun save(
                scheduleId: ScheduleId,
                targetCompleted: Boolean
            ): Result<ScheduleCompletionBacklog> = scheduleCompletionBacklogRepository
                .save(scheduleId, targetCompleted)
                .onFailure { Timber.e(it) }
        },
        registerScheduleCompleteJob = RegisterScheduleCompleteJobUseCaseImpl(context),
        delayTime = 500.milliseconds
    )
}