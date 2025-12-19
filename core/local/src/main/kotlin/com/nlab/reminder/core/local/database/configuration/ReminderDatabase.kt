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

package com.nlab.reminder.core.local.database.configuration

import android.content.Context
import androidx.room.*
import com.nlab.reminder.core.local.database.dao.LinkMetadataDAO
import com.nlab.reminder.core.local.database.dao.RepeatDetailDAO
import com.nlab.reminder.core.local.database.dao.ScheduleCompletionBacklogDAO
import com.nlab.reminder.core.local.database.dao.ScheduleDAO
import com.nlab.reminder.core.local.database.dao.ScheduleRepeatDetailDAO
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.dao.TagDAO
import com.nlab.reminder.core.local.database.entity.LinkMetadataEntity
import com.nlab.reminder.core.local.database.entity.RepeatDetailEntity
import com.nlab.reminder.core.local.database.entity.ScheduleCompletionBacklogEntity
import com.nlab.reminder.core.local.database.entity.ScheduleEntity
import com.nlab.reminder.core.local.database.entity.ScheduleTagListEntity
import com.nlab.reminder.core.local.database.entity.TagEntity
import com.nlab.reminder.core.local.database.util.InstantConverter

/**
 * @author Doohyun
 */
private const val DB_NAME = "reminder_common.db"

@Database(
    entities = [
        LinkMetadataEntity::class,
        RepeatDetailEntity::class,
        ScheduleCompletionBacklogEntity::class,
        ScheduleEntity::class,
        ScheduleTagListEntity::class,
        TagEntity::class
    ],
    version = 1
)
@TypeConverters(
    value = [
        InstantConverter::class
    ]
)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun linkMetadataDAO(): LinkMetadataDAO
    abstract fun repeatDetailDAO(): RepeatDetailDAO
    abstract fun scheduleCompletionBacklogDAO(): ScheduleCompletionBacklogDAO
    abstract fun scheduleDAO(): ScheduleDAO
    abstract fun scheduleRepeatDetailDAO(): ScheduleRepeatDetailDAO
    abstract fun scheduleTagListDAO(): ScheduleTagListDAO
    abstract fun tagDAO(): TagDAO
}

fun ReminderDatabase(context: Context): ReminderDatabase =
    Room.databaseBuilder(context, ReminderDatabase::class.java, DB_NAME).build()