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
import com.nlab.reminder.core.local.database.dao.ScheduleDAO
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.dao.TagDAO
import com.nlab.reminder.core.local.database.model.LinkMetadataEntity
import com.nlab.reminder.core.local.database.model.RepeatDetailEntity
import com.nlab.reminder.core.local.database.model.ScheduleEntity
import com.nlab.reminder.core.local.database.model.ScheduleTagListEntity
import com.nlab.reminder.core.local.database.model.TagEntity
import com.nlab.reminder.core.local.database.util.InstantConverter

/**
 * @author Doohyun
 */
private const val DB_NAME = "reminder_common.db"

@Database(
    entities = [
        LinkMetadataEntity::class,
        RepeatDetailEntity::class,
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
internal abstract class ReminderDatabase : RoomDatabase() {
    abstract fun linkMetadataDAO(): LinkMetadataDAO
    abstract fun repeatDetailDAO(): RepeatDetailDAO
    abstract fun scheduleDAO(): ScheduleDAO
    abstract fun scheduleTagListDAO(): ScheduleTagListDAO
    abstract fun tagDAO(): TagDAO
}

internal fun ReminderDatabase(context: Context): ReminderDatabase =
    Room.databaseBuilder(context, ReminderDatabase::class.java, DB_NAME).build()