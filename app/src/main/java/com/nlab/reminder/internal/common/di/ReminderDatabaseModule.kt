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

package com.nlab.reminder.internal.common.di

import android.app.Application
import com.nlab.reminder.internal.common.android.database.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
class ReminderDatabaseModule {
    @Singleton
    @Provides
    fun provideReminderDatabase(application: Application): ReminderDatabase = ReminderDatabase.getDatabase(application)

    @Singleton
    @Provides
    fun provideLinkMetadataDao(reminderDatabase: ReminderDatabase): LinkMetadataDao = reminderDatabase.linkMetadataDao()

    @Singleton
    @Provides
    fun provideScheduleDao(reminderDatabase: ReminderDatabase): ScheduleDao = reminderDatabase.scheduleDao()

    @Singleton
    @Provides
    fun provideScheduleTagListDao(
        reminderDatabase: ReminderDatabase
    ): ScheduleTagListDao = reminderDatabase.scheduleTagListDao()

    @Singleton
    @Provides
    fun provideTagDao(reminderDatabase: ReminderDatabase): TagDao = reminderDatabase.tagDao()
}