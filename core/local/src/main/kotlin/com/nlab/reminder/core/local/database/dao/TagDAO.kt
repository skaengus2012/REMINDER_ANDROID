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

package com.nlab.reminder.core.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.nlab.reminder.core.local.database.model.TagEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * @author Doohyun
 */
@Dao
abstract class TagDAO {
    @Insert
    protected abstract suspend fun insert(entity: TagEntity): Long

    @Query("SELECT * FROM tag WHERE tag_id = :tagId")
    protected abstract suspend fun findById(tagId: Long): TagEntity?

    @Query("SELECT * FROM tag WHERE name = :tagName")
    abstract suspend fun findByName(tagName: String): TagEntity?

    @Query("SELECT * FROM tag")
    abstract fun getAsStream(): Flow<Array<TagEntity>>

    @Query("SELECT * FROM tag WHERE tag_id IN (:tagIds)")
    protected abstract fun findByIdsAsStreamInternal(tagIds: Set<Long>): Flow<Array<TagEntity>>

    fun findByIdsAsStream(tagIds: Set<Long>): Flow<Array<TagEntity>> =
        if (tagIds.isEmpty()) flowOf(emptyArray())
        else findByIdsAsStreamInternal(tagIds)

    @Update
    protected abstract suspend fun update(entity: TagEntity)

    @Query("DELETE FROM tag WHERE tag_id = :tagId")
    abstract suspend fun deleteById(tagId: Long)

    @Transaction
    open suspend fun insertAndGet(name: String): TagEntity {
        return checkNotNull(findById(tagId = insert(TagEntity(name = name))))
    }

    @Transaction
    open suspend fun updateAndGet(tagId: Long, name: String): TagEntity {
        val oldEntity = checkNotNull(findById(tagId))
        val newEntity = TagEntity(tagId = tagId, name = name)
        if (oldEntity == newEntity) return oldEntity // No changes

        update(newEntity)
        return newEntity
    }
}