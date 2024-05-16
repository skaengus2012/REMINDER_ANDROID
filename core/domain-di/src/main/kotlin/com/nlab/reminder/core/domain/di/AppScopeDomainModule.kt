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

package com.nlab.reminder.core.domain.di

import com.nlab.reminder.core.data.repository.LinkMetadataTableRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.di.coroutine.Dispatcher
import com.nlab.reminder.core.di.coroutine.DispatcherOption
import com.nlab.reminder.core.domain.CalculateItemSwapResultUseCase
import com.nlab.reminder.core.domain.CompleteScheduleWithIdsUseCase
import com.nlab.reminder.core.domain.FetchLinkMetadataUseCase
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
internal class AppScopeDomainModule {
    @Provides
    @Reusable
    fun provideCalculateItemSwapResultUseCase(): CalculateItemSwapResultUseCase =
        CalculateItemSwapResultUseCase()

    @Provides
    @Reusable
    fun provideCompleteScheduleWithIdsUseCase(
        scheduleRepository: ScheduleRepository,
        @Dispatcher(DispatcherOption.Default) dispatcher: CoroutineDispatcher
    ): CompleteScheduleWithIdsUseCase = CompleteScheduleWithIdsUseCase(scheduleRepository, dispatcher)

    @Provides
    @Reusable
    fun provideFetchLinkMetadataUseCase(
        linkMetadataTableRepository: LinkMetadataTableRepository,
        @Dispatcher(DispatcherOption.Default) dispatcher: CoroutineDispatcher
    ): FetchLinkMetadataUseCase = FetchLinkMetadataUseCase(linkMetadataTableRepository, dispatcher)

}