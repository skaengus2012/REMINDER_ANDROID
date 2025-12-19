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

package com.nlab.reminder.core.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * @author Doohyun
 */
@Entity(
    tableName = "schedule_completion_backlog",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["schedule_id"],
            childColumns = ["schedule_id"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class ScheduleCompletionBacklogEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "schedule_completion_backlog_id") val backlogId: Long = EMPTY_GENERATED_ID,
    @ColumnInfo(name = "schedule_id", index = true) val scheduleId: Long,
    @ColumnInfo(name = "target_completed") val targetCompleted: Boolean,
    @ColumnInfo(name = "insert_order") val insertOrder: Int
)