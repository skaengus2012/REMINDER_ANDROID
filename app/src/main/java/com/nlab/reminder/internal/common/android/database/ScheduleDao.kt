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

package com.nlab.reminder.internal.common.android.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * @author Doohyun
 */
@Dao
interface ScheduleDao {
    @Insert(onConflict = REPLACE)
    suspend fun insert(schedule: ScheduleEntity): Long

    @Transaction
    @Query("SELECT * FROM schedule WHERE is_complete = :isComplete")
    fun find(isComplete: Boolean): Flow<List<ScheduleEntityWithTagEntities>>

    @Transaction
    @Query("SELECT * FROM schedule WHERE is_complete = :isComplete")
    fun findAsPagingSource(isComplete: Boolean): PagingSource<Int, ScheduleEntityWithTagEntities>
}