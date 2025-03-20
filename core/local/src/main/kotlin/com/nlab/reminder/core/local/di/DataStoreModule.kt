/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.core.local.di

import android.content.Context
import com.nlab.reminder.core.inject.qualifiers.coroutine.AppScope
import com.nlab.reminder.core.inject.qualifiers.coroutine.Dispatcher
import com.nlab.reminder.core.inject.qualifiers.coroutine.DispatcherOption.IO
import com.nlab.reminder.core.local.datastore.configuration.ReminderPreferenceDataStore
import com.nlab.reminder.core.local.datastore.preference.PreferenceDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import javax.inject.Singleton

/**
 * @author Thalys
 */
@Module
@InstallIn(SingletonComponent::class)
internal class DataStoreModule {
    @Singleton
    @Provides
    fun providePreferenceDataSource(
        @ApplicationContext context: Context,
        @AppScope applicationScope: CoroutineScope,
        @Dispatcher(IO) dispatcher: CoroutineDispatcher
    ): PreferenceDataSource = PreferenceDataSource(
        dataStore = ReminderPreferenceDataStore(
            context = context,
            coroutineScope = applicationScope + dispatcher
        )
    )
}