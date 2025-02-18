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
import com.nlab.reminder.core.local.database.configuration.ReminderDatabase
import com.nlab.reminder.core.local.database.dao.LinkMetadataDAO
import com.nlab.reminder.core.local.database.dao.RepeatDetailDAO
import com.nlab.reminder.core.local.database.dao.ScheduleDAO
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.dao.TagDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author Thalys
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class DatabaseModule {
    @Singleton
    @Provides
    fun provideReminderDatabase(
        @ApplicationContext context: Context
    ): ReminderDatabase = ReminderDatabase(context)

    @Provides
    fun provideLinkMetadataDAO(
        reminderDatabase: ReminderDatabase
    ): LinkMetadataDAO = reminderDatabase.linkMetadataDAO()

    @Provides
    fun provideScheduleDAO(
        reminderDatabase: ReminderDatabase
    ): ScheduleDAO = reminderDatabase.scheduleDAO()

    @Provides
    fun provideScheduleTagListDAO(
        reminderDatabase: ReminderDatabase
    ): ScheduleTagListDAO = reminderDatabase.scheduleTagListDAO()

    @Provides
    fun provideTagDAO(
        reminderDatabase: ReminderDatabase
    ): TagDAO = reminderDatabase.tagDAO()

    @Provides
    fun provideRepeatDetailDAO(
        reminderDatabase: ReminderDatabase
    ): RepeatDetailDAO = reminderDatabase.repeatDetailDAO()
}