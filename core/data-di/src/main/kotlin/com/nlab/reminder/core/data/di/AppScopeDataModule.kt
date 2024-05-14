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

import com.nlab.reminder.core.data.model.Link
import com.nlab.reminder.core.data.model.LinkMetadata
import com.nlab.reminder.core.data.repository.LinkMetadataRepository
import com.nlab.reminder.core.data.repository.TimestampRepository
import com.nlab.reminder.core.data.repository.impl.infra.DefaultTimestampRepository
import com.nlab.reminder.core.data.repository.impl.infra.JsoupLinkMetadataRepository
import com.nlab.reminder.core.di.coroutine.Dispatcher
import com.nlab.reminder.core.di.coroutine.DispatcherOption.IO
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.onFailure
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import timber.log.Timber
import com.nlab.reminder.core.data.di.ScheduleDataOption.*
import com.nlab.reminder.core.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.core.data.repository.impl.CompletedScheduleShownRepositoryImpl
import com.nlab.reminder.core.local.datastore.PreferenceDataSource

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
internal class AppScopeDataModule {
    @Provides
    @Reusable
    fun provideLinkMetadataRepository(
        @Dispatcher(IO) dispatcher: CoroutineDispatcher
    ): LinkMetadataRepository = object : LinkMetadataRepository {
        private val internalRepository = JsoupLinkMetadataRepository(dispatcher = dispatcher)
        override suspend fun get(link: Link): Result<LinkMetadata> {
            return internalRepository.get(link).onFailure {
                Timber.w(it, "LinkMetadata load failed.")
            }
        }
    }

    @Provides
    @Reusable
    fun provideTimestampRepository(): TimestampRepository = DefaultTimestampRepository()

    @Provides
    @Reusable
    @ScheduleData(All)
    fun provideCompletedScheduleShownRepository(
        preferenceDataSource: PreferenceDataSource
    ): CompletedScheduleShownRepository = CompletedScheduleShownRepositoryImpl(
        getAsStreamFunction = { preferenceDataSource.getAllScheduleCompleteShownAsStream() },
        setShownFunction = { preferenceDataSource.setAllScheduleCompleteShown(it) }
    )
}