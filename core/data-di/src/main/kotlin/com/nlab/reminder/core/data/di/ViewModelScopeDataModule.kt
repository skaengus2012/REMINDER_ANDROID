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
import com.nlab.reminder.core.data.repository.impl.InMemoryLinkMetadataCache
import com.nlab.reminder.core.data.repository.impl.InMemoryScheduleCompleteMarkRepository
import com.nlab.reminder.core.data.repository.impl.OfflineFirstLinkMetadataRepository
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.onFailure
import com.nlab.reminder.core.kotlin.onSuccess
import com.nlab.reminder.core.local.database.dao.LinkMetadataDAO
import com.nlab.reminder.core.network.datasource.LinkThumbnailDataSource
import com.nlab.reminder.core.network.datasource.LinkThumbnailResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import timber.log.Timber

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
        linkThumbnailDataSource: LinkThumbnailDataSource
    ): LinkMetadataRepository = OfflineFirstLinkMetadataRepository(
        linkMetadataDAO = linkMetadataDAO,
        linkThumbnailDataSource = object : LinkThumbnailDataSource {
            override suspend fun getLinkThumbnail(
                url: NonBlankString
            ): Result<LinkThumbnailResponse> = linkThumbnailDataSource.getLinkThumbnail(url)
                .onSuccess { response ->
                    Timber.d("The linkMetadata loading success -> [$url : $response]")
                }
                .onFailure { e -> Timber.w(e, "The linkMetadata loading failed -> [$url]") }

        },
        inMemoryCache = InMemoryLinkMetadataCache(initialCache = emptyMap())
    )

    @Provides
    @ViewModelScoped
    fun provideScheduleCompleteMarkRepository(): ScheduleCompleteMarkRepository =
        InMemoryScheduleCompleteMarkRepository()
}