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

package com.nlab.reminder.core.local.di

import android.app.Application
import com.nlab.reminder.core.local.database.LinkMetadataDao
import com.nlab.reminder.core.local.database.ReminderDatabase
import com.nlab.reminder.core.local.database.ScheduleDao
import com.nlab.reminder.core.local.database.ScheduleTagListDao
import com.nlab.reminder.core.local.database.TagDao
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
internal class DatabaseModule {
    @Singleton
    @Provides
    fun provideReminderDatabase(application: Application): ReminderDatabase = ReminderDatabase.getDatabase(application)

    @Provides
    fun provideLinkMetadataDao(reminderDatabase: ReminderDatabase): LinkMetadataDao = reminderDatabase.linkMetadataDao()

    @Provides
    fun provideScheduleDao(reminderDatabase: ReminderDatabase): ScheduleDao = reminderDatabase.scheduleDao()

    @Provides
    fun provideScheduleTagListDao(
        reminderDatabase: ReminderDatabase
    ): ScheduleTagListDao = reminderDatabase.scheduleTagListDao()

    @Provides
    fun provideTagDao(reminderDatabase: ReminderDatabase): TagDao = reminderDatabase.tagDao()
}