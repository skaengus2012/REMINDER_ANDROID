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

package com.nlab.reminder.core.local.database

import android.content.Context
import androidx.room.*

/**
 * @author Doohyun
 */
@Database(
    entities = [
        LinkMetadataEntity::class,
        ScheduleEntity::class,
        ScheduleTagListEntity::class,
        TagEntity::class
    ],
    version = 1
)
@TypeConverters(
    value = [
        StringValuesConverter::class
    ]
)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun tagDao(): TagDao
    abstract fun scheduleTagListDao(): ScheduleTagListDao
    abstract fun linkMetadataDao(): LinkMetadataDao

    companion object {
        private const val DB_NAME = "reminder_common.db"

        fun getDatabase(context: Context): ReminderDatabase =
            Room.databaseBuilder(context, ReminderDatabase::class.java, DB_NAME).build()
    }
}