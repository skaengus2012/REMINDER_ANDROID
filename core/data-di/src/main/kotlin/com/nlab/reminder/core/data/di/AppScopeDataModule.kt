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

import android.content.Context
import com.nlab.reminder.core.data.qualifiers.ScheduleData
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.nlab.reminder.core.data.qualifiers.ScheduleDataOption.*
import com.nlab.reminder.core.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.core.data.repository.LinkMetadataRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.data.repository.impl.CompletedScheduleShownRepositoryImpl
import com.nlab.reminder.core.data.repository.impl.LinkMetadataRemoteCache
import com.nlab.reminder.core.data.repository.impl.LocalScheduleRepository
import com.nlab.reminder.core.data.repository.impl.LocalTagRepository
import com.nlab.reminder.core.data.repository.impl.OfflineFirstLinkMetadataRepository
import com.nlab.reminder.core.data.util.SystemTimeChangedMonitor
import com.nlab.reminder.core.data.util.SystemTimeZoneMonitor
import com.nlab.reminder.core.data.util.TimeChangedMonitor
import com.nlab.reminder.core.data.util.TimeZoneMonitor
import com.nlab.reminder.core.inject.qualifiers.coroutine.AppScope
import com.nlab.reminder.core.inject.qualifiers.coroutine.Dispatcher
import com.nlab.reminder.core.inject.qualifiers.coroutine.DispatcherOption.*
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.onFailure
import com.nlab.reminder.core.kotlin.onSuccess
import com.nlab.reminder.core.kotlin.toPositiveInt
import com.nlab.reminder.core.local.database.dao.LinkMetadataDAO
import com.nlab.reminder.core.local.database.dao.ScheduleDAO
import com.nlab.reminder.core.local.database.dao.ScheduleRepeatDetailDAO
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.dao.TagDAO
import com.nlab.reminder.core.local.database.transaction.InsertAndGetScheduleContentAggregateTransaction
import com.nlab.reminder.core.local.database.transaction.UpdateAndGetScheduleContentAggregateTransaction
import com.nlab.reminder.core.local.database.transaction.UpdateOrMergeAndGetTagTransaction
import com.nlab.reminder.core.local.datastore.preference.PreferenceDataSource
import com.nlab.reminder.core.network.datasource.LinkThumbnailDataSource
import com.nlab.reminder.core.network.datasource.LinkThumbnailResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import javax.inject.Singleton

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
internal object AppScopeDataModule {
    @ScheduleData(All)
    @Reusable
    @Provides
    fun provideCompletedScheduleShownRepository(
        preferenceDataSource: PreferenceDataSource
    ): CompletedScheduleShownRepository = CompletedScheduleShownRepositoryImpl(
        getAsStreamFunction = { preferenceDataSource.getAllScheduleCompleteShownAsStream() },
        setShownFunction = { preferenceDataSource.setAllScheduleCompleteShown(it) }
    )

    @Reusable
    @Provides
    fun provideScheduleRepository(
        scheduleDAO: ScheduleDAO,
        scheduleRepeatDetailDAO: ScheduleRepeatDetailDAO,
        scheduleTagListDAO: ScheduleTagListDAO,
        insertAndGetScheduleContentAggregateTransaction: InsertAndGetScheduleContentAggregateTransaction,
        updateAndGetScheduleContentAggregateTransaction: UpdateAndGetScheduleContentAggregateTransaction
    ): ScheduleRepository = LocalScheduleRepository(
        scheduleDAO = scheduleDAO,
        scheduleRepeatDetailDAO = scheduleRepeatDetailDAO,
        scheduleTagListDAO = scheduleTagListDAO,
        insertAndGetScheduleContentAggregate = insertAndGetScheduleContentAggregateTransaction,
        updateAndGetScheduleContentAggregate = updateAndGetScheduleContentAggregateTransaction
    )

    @Reusable
    @Provides
    fun provideTagRepository(
        tagDAO: TagDAO,
        scheduleTagListDAO: ScheduleTagListDAO,
        updateOrReplaceAndGetTag: UpdateOrMergeAndGetTagTransaction
    ): TagRepository = LocalTagRepository(
        tagDAO = tagDAO,
        scheduleTagListDAO = scheduleTagListDAO,
        updateOrMergeAndGetTag = updateOrReplaceAndGetTag
    )

    @Singleton
    @Provides
    fun provideLinkMetadataRemoteCache(): LinkMetadataRemoteCache = LinkMetadataRemoteCache(
        cacheSize = 5000.toPositiveInt()
    )

    @Reusable
    @Provides
    fun provideLinkMetadataTableRepository(
        linkMetadataDAO: LinkMetadataDAO,
        linkThumbnailDataSource: LinkThumbnailDataSource,
        linkMetadataRemoteCache: LinkMetadataRemoteCache
    ): LinkMetadataRepository = OfflineFirstLinkMetadataRepository(
        linkMetadataDAO = linkMetadataDAO,
        remoteDataSource = object : LinkThumbnailDataSource {
            override suspend fun getLinkThumbnail(
                url: NonBlankString
            ): Result<LinkThumbnailResponse> = linkThumbnailDataSource.getLinkThumbnail(url)
                .onSuccess { response ->
                    Timber.d("The linkMetadata loading success -> [$url : $response]")
                }
                .onFailure { e -> Timber.w(e, "The linkMetadata loading failed -> [$url]") }

        },
        remoteCache = linkMetadataRemoteCache
    )

    @Singleton
    @Provides
    fun provideTimeZoneMonitor(
        @ApplicationContext context: Context,
        @AppScope coroutineScope: CoroutineScope,
        @Dispatcher(IO) dispatcher: CoroutineDispatcher
    ): TimeZoneMonitor = SystemTimeZoneMonitor(
        context = context,
        coroutineScope = coroutineScope,
        dispatcher = dispatcher
    )

    @Singleton
    @Provides
    fun provideTimeChangedMonitor(
        @ApplicationContext context: Context,
        @AppScope coroutineScope: CoroutineScope,
        @Dispatcher(IO) dispatcher: CoroutineDispatcher
    ): TimeChangedMonitor = SystemTimeChangedMonitor(
        context = context,
        coroutineScope = coroutineScope,
        dispatcher = dispatcher
    )
}