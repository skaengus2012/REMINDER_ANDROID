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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * @author Doohyun
 */
@Dao
interface TagDao {
    @Insert
    suspend fun insert(tagEntity: TagEntity): Long

    @Update
    suspend fun update(tagEntity: TagEntity)

    @Query("DELETE FROM tag WHERE tag_id = :tagId")
    suspend fun deleteById(tagId: Long)

    @Query("SELECT * FROM tag WHERE tag_id = :tagId")
    suspend fun findById(tagId: Long): TagEntity?

    @Query("SELECT * FROM tag WHERE name = :name")
    suspend fun findByName(name: String): TagEntity?

    @Query("SELECT * FROM tag")
    fun getAsStream(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tag WHERE tag_id IN (:tagIds)")
    fun findByIdsAsStream(tagIds: List<Long>): Flow<TagEntity>
}