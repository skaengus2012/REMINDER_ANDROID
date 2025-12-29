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
import com.nlab.reminder.core.data.repository.ScheduleCompletionBacklogRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.SystemTimeSnapshotRepository
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.data.repository.TimeSnapshotRepository
import com.nlab.reminder.core.data.repository.impl.DefaultCompletedScheduleShownRepository
import com.nlab.reminder.core.data.repository.impl.LinkMetadataRemoteCache
import com.nlab.reminder.core.data.repository.impl.LocalScheduleCompletionBacklogRepository
import com.nlab.reminder.core.data.repository.impl.LocalScheduleRepository
import com.nlab.reminder.core.data.repository.impl.LocalTagRepository
import com.nlab.reminder.core.data.repository.impl.OfflineFirstLinkMetadataRepository
import com.nlab.reminder.core.data.repository.impl.RemoteFirstTimeSnapshotRepository
import com.nlab.reminder.core.data.util.SystemTimeChangedMonitor
import com.nlab.reminder.core.data.util.SystemTimeZoneMonitor
import com.nlab.reminder.core.data.util.TimeZoneMonitor
import com.nlab.reminder.core.inject.qualifiers.coroutine.AppScope
import com.nlab.reminder.core.inject.qualifiers.coroutine.Dispatcher
import com.nlab.reminder.core.inject.qualifiers.coroutine.DispatcherOption.*
import com.nlab.reminder.core.kotlin.onFailure
import com.nlab.reminder.core.kotlin.onSuccess
import com.nlab.reminder.core.kotlin.toPositiveInt
import com.nlab.reminder.core.local.database.dao.LinkMetadataDAO
import com.nlab.reminder.core.local.database.dao.ScheduleCompletionBacklogDAO
import com.nlab.reminder.core.local.database.dao.ScheduleDAO
import com.nlab.reminder.core.local.database.dao.ScheduleRepeatDetailDAO
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.dao.TagDAO
import com.nlab.reminder.core.local.database.transaction.InsertAndGetScheduleContentAggregateTransaction
import com.nlab.reminder.core.local.database.transaction.UpdateAndGetScheduleContentAggregateTransaction
import com.nlab.reminder.core.local.database.transaction.UpdateOrMergeAndGetTagTransaction
import com.nlab.reminder.core.local.datastore.preference.PreferenceDataSource
import com.nlab.reminder.core.network.datasource.LinkThumbnailDataSource
import com.nlab.reminder.core.network.datasource.TrustedTimeDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

import timber.log.Timber

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
internal object AppScopeDataModule {
    @ScheduleData(All)
    @Reusable
    @Provides
    fun provideAllCompletedScheduleShownRepository(
        preferenceDataSource: PreferenceDataSource
    ): CompletedScheduleShownRepository = DefaultCompletedScheduleShownRepository(
        getAsStreamFunction = { preferenceDataSource.getAllScheduleCompleteShownAsStream() },
        setShownFunction = { preferenceDataSource.setAllScheduleCompleteShown(it) }
    )

    @Reusable
    @Provides
    fun provideScheduleCompletionBacklogRepository(
        scheduleCompletionBacklogDAO: ScheduleCompletionBacklogDAO,
    ): ScheduleCompletionBacklogRepository = LocalScheduleCompletionBacklogRepository(
        scheduleCompletionBacklogDAO = scheduleCompletionBacklogDAO
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
        remoteDataSource = { url ->
            linkThumbnailDataSource.getLinkThumbnail(url)
                .onSuccess { response ->
                    Timber.remoteSourceTag().d(message = "The linkMetadata loading success -> [$url : $response]")
                }
                .onFailure { e ->
                    Timber.remoteSourceTag().w(e, message = "The linkMetadata loading failed -> [$url]")
                }
        },
        remoteCache = linkMetadataRemoteCache
    )

    @Reusable
    @Provides
    fun provideRemoteFirstTimeSnapshotRepository(
        trustedTimeDataSource: TrustedTimeDataSource,
        systemTimeChangedMonitor: SystemTimeChangedMonitor,
    ): TimeSnapshotRepository = RemoteFirstTimeSnapshotRepository(
        trustedTimeDataSource = {
            trustedTimeDataSource.getCurrentTime()
                .onSuccess { instant -> Timber.remoteSourceTag().d("The trusted time success -> [$instant]") }
                .onFailure { e -> Timber.remoteSourceTag().w(e) }
        },
        fallbackSnapshotRepository = SystemTimeSnapshotRepository(systemTimeChangedMonitor = systemTimeChangedMonitor)
    )

    @Singleton
    @Provides
    fun provideSystemTimeChangeMonitor(
        @ApplicationContext context: Context,
        @AppScope coroutineScope: CoroutineScope,
        @Dispatcher(IO) dispatcher: CoroutineDispatcher
    ) = SystemTimeChangedMonitor(
        context = context,
        coroutineScope = coroutineScope,
        dispatcher = dispatcher
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
}