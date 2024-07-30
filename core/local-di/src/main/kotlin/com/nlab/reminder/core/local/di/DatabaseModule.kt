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

import android.content.Context
import com.nlab.reminder.core.local.database.dao.LinkMetadataDAO
import com.nlab.reminder.core.local.database.configuration.ReminderDatabase
import com.nlab.reminder.core.local.database.dao.ScheduleDAO
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.dao.TagRelationDAO
import com.nlab.reminder.core.local.database.dao.TagDAO
import com.nlab.reminder.core.local.database.util.TransactionScope
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun provideReminderDatabase(
        @ApplicationContext context: Context
    ): ReminderDatabase = ReminderDatabase.getDatabase(context)

    @Reusable
    @Provides
    fun provideTransactionScope(
        reminderDatabase: ReminderDatabase
    ): TransactionScope = TransactionScope(reminderDatabase)

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
    fun provideTagRelationDAO(
        transactionScope: TransactionScope,
        tagDAO: TagDAO,
        scheduleTagListDAO: ScheduleTagListDAO
    ): TagRelationDAO = TagRelationDAO(
        transactionScope = transactionScope,
        tagDAO = tagDAO,
        scheduleTagListDAO = scheduleTagListDAO
    )

    @Provides
    fun provideTagDAO(
        reminderDatabase: ReminderDatabase
    ): TagDAO = reminderDatabase.tagDAO()
}