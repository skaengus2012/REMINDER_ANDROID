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

package com.nlab.reminder.core.data.di

import com.nlab.reminder.core.data.repository.LinkMetadataRepository
import com.nlab.reminder.core.data.repository.ScheduleCompleteMarkRepository
import com.nlab.reminder.core.data.repository.ScheduleSelectedIdRepository
import com.nlab.reminder.core.data.repository.impl.InMemoryScheduleCompleteMarkRepository
import com.nlab.reminder.core.data.repository.impl.InMemoryScheduleSelectedIdRepository
import com.nlab.reminder.core.data.repository.impl.OfflineFirstLinkMetadataRepository
import com.nlab.reminder.core.data.util.TimestampProvider
import com.nlab.reminder.core.inject.qualifiers.coroutine.Dispatcher
import com.nlab.reminder.core.inject.qualifiers.coroutine.DispatcherOption.*
import com.nlab.reminder.core.local.database.dao.LinkMetadataDAO
import com.nlab.reminder.core.network.LinkThumbnailDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineDispatcher

/**
 * @author Doohyun
 */
@Module
@InstallIn(ViewModelComponent::class)
internal class ViewModelScopeDataModule {
    @ViewModelScoped
    @Provides
    fun provideCachedLinkMetadataTableRepository(
        linkMetadataDAO: LinkMetadataDAO,
        timestampProvider: TimestampProvider,
        @Dispatcher(IO) remoteDispatcher: CoroutineDispatcher,
    ): LinkMetadataRepository = OfflineFirstLinkMetadataRepository(
        linkMetadataDAO = linkMetadataDAO,
        linkThumbnailDataSource = LinkThumbnailDataSourceImpl(remoteDispatcher),
        timestampProvider = timestampProvider,
        initialCache = emptyMap()
    )

    @Provides
    @ViewModelScoped
    fun provideScheduleCompleteMarkRepository(): ScheduleCompleteMarkRepository =
        InMemoryScheduleCompleteMarkRepository()

    @Provides
    @ViewModelScoped
    fun provideScheduleSelectedIdRepository(): ScheduleSelectedIdRepository =
        InMemoryScheduleSelectedIdRepository()
}