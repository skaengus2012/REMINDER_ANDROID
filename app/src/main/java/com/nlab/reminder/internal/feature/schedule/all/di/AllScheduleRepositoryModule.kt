/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.internal.feature.schedule.all.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.nlab.reminder.BuildConfig
import com.nlab.reminder.domain.common.schedule.visibleconfig.CompletedScheduleShownRepository
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleScope
import com.nlab.reminder.internal.common.schedule.visibleconfig.impl.LocalCompletedScheduleShownRepository
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
class AllScheduleRepositoryModule {
    @AllScheduleScope
    @Reusable
    @Provides
    fun provideDoneScheduleShownRepository(
        dataStore: DataStore<Preferences>
    ): CompletedScheduleShownRepository = LocalCompletedScheduleShownRepository(
        dataStore,
        booleanPreferencesKey(BuildConfig.PREFERENCE_KEY_ALL_SCHEDULE_COMPLETE_SHOWN)
    )
}