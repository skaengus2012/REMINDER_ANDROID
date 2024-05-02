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

import com.nlab.reminder.core.data.repository.impl.CachedLinkMetadataTableRepository
import com.nlab.reminder.core.data.repository.impl.InMemoryScheduleCompleteMarkRepository
import com.nlab.reminder.core.data.repository.impl.InMemoryScheduleSelectedIdRepository
import com.nlab.reminder.core.data.repository.LinkMetadataTableRepository
import com.nlab.reminder.core.data.repository.ScheduleCompleteMarkRepository
import com.nlab.reminder.core.data.repository.ScheduleSelectedIdRepository
import com.nlab.reminder.domain.common.kotlin.coroutine.inject.DefaultDispatcher
import com.nlab.reminder.internal.data.repository.LocalLinkMetadataTableRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineDispatcher

/**
 * @author thalys
 */
@Module
@InstallIn(ViewModelComponent::class)
internal abstract class ViewModelScopeRepositoryModule {
    @ViewModelScoped
    @Binds
    abstract fun bindScheduleCompleteMarkRepository(
        repository: InMemoryScheduleCompleteMarkRepository
    ): ScheduleCompleteMarkRepository

    @ViewModelScoped
    @Binds
    abstract fun bindScheduleSelectedIdRepository(
        repository: InMemoryScheduleSelectedIdRepository
    ): ScheduleSelectedIdRepository

    companion object {
        @ViewModelScoped
        @Provides
        fun provideCachedLinkMetadataTableRepository(
            repository: LocalLinkMetadataTableRepository,
            @DefaultDispatcher dispatcher: CoroutineDispatcher
        ): LinkMetadataTableRepository = CachedLinkMetadataTableRepository(repository, dispatcher)
    }
}