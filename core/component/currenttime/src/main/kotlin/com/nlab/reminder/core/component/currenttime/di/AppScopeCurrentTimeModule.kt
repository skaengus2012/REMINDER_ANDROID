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

package com.nlab.reminder.core.component.currenttime.di

import com.nlab.reminder.core.component.currenttime.GetCurrentTimeSnapshotStreamUseCase
import com.nlab.reminder.core.component.currenttime.impl.SystemTimeUsageBroadcastMonitor
import com.nlab.reminder.core.data.repository.TimeSnapshotRepository
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
internal object AppScopeCurrentTimeModule {
    @Singleton
    @Provides
    fun provideSystemTimeUsageBroadcastMonitor(): SystemTimeUsageBroadcastMonitor = SystemTimeUsageBroadcastMonitor()

    @Reusable
    @Provides
    fun provideGetCurrentTimeSnapshotUseCase(
        timeSnapshotRepository: TimeSnapshotRepository,
        systemTimeUsageBroadcastMonitor: SystemTimeUsageBroadcastMonitor,
    ): GetCurrentTimeSnapshotStreamUseCase = GetCurrentTimeSnapshotStreamUseCase(
        timeSnapshotRepository = timeSnapshotRepository,
        systemTimeUsageBroadcast = systemTimeUsageBroadcastMonitor
    )
}