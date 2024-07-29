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

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.nlab.reminder.core.data.di.ScheduleDataOption.*
import com.nlab.reminder.core.data.model.impl.CachedTagFactory
import com.nlab.reminder.core.data.model.impl.DefaultTagFactory
import com.nlab.reminder.core.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.data.repository.impl.CompletedScheduleShownRepositoryImpl
import com.nlab.reminder.core.data.repository.impl.LocalScheduleRepository
import com.nlab.reminder.core.data.repository.impl.LocalTagRepository
import com.nlab.reminder.core.foundation.cache.CacheFactory
import com.nlab.reminder.core.local.database.ScheduleDao
import com.nlab.reminder.core.local.database.ScheduleTagListDao
import com.nlab.reminder.core.local.database.TagDao
import com.nlab.reminder.core.local.datastore.PreferenceDataSource

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
internal class AppScopeDataModule {
    @Provides
    @Reusable
    @ScheduleData(All)
    fun provideCompletedScheduleShownRepository(
        preferenceDataSource: PreferenceDataSource
    ): CompletedScheduleShownRepository = CompletedScheduleShownRepositoryImpl(
        getAsStreamFunction = { preferenceDataSource.getAllScheduleCompleteShownAsStream() },
        setShownFunction = { preferenceDataSource.setAllScheduleCompleteShown(it) }
    )

    @Provides
    @Reusable
    fun provideScheduleRepository(
        scheduleDao: ScheduleDao
    ): ScheduleRepository = LocalScheduleRepository(scheduleDao)

    @Provides
    @Reusable
    fun provideTagRepository(
        tagDao: TagDao,
        scheduleTagListDao: ScheduleTagListDao,
        cacheFactory: CacheFactory,
    ): TagRepository = LocalTagRepository(
        tagDao,
        scheduleTagListDao,
        CachedTagFactory(
            internalFactory = DefaultTagFactory(),
            cacheFactory = cacheFactory,
            maxSize = 1000
        )
    )
}