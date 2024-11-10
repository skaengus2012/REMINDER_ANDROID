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

import com.nlab.reminder.core.data.qualifiers.ScheduleData
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.nlab.reminder.core.data.qualifiers.ScheduleDataOption.*
import com.nlab.reminder.core.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.data.repository.impl.CompletedScheduleShownRepositoryImpl
import com.nlab.reminder.core.data.repository.impl.LocalScheduleRepository
import com.nlab.reminder.core.data.repository.impl.LocalTagRepository
import com.nlab.reminder.core.local.database.dao.ScheduleDAO
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.dao.TagRelationDAO
import com.nlab.reminder.core.local.database.dao.TagDAO
import com.nlab.reminder.core.local.datastore.PreferenceDataSource

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
internal class AppScopeRepositoryModule {
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
        scheduleDao: ScheduleDAO,
    ): ScheduleRepository = LocalScheduleRepository(scheduleDAO = scheduleDao)

    @Provides
    @Reusable
    fun provideTagRepository(
        tagDao: TagDAO,
        tagRelationDAO: TagRelationDAO,
        scheduleTagListDao: ScheduleTagListDAO,
    ): TagRepository = LocalTagRepository(
        tagDAO = tagDao,
        tagRelationDAO = tagRelationDAO,
        scheduleTagListDAO = scheduleTagListDao,
    )
}