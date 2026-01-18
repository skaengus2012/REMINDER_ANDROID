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
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.local.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * @author Doohyun
 */
@Dao
abstract class TagDAO {
    @Insert
    protected abstract suspend fun insert(entity: TagEntity): Long

    suspend fun insertAndGet(name: NonBlankString): TagEntity {
        val newEntity = TagEntity(name = name.value)
        return newEntity.copy(tagId = insert(newEntity))
    }

    @Insert
    protected abstract suspend fun insert(entities: List<TagEntity>): List<Long>

    @Transaction
    open suspend fun insertAndGet(names: Set<NonBlankString>): List<TagEntity> {
        if (names.isEmpty()) return emptyList()

        val currentNameToEntityTable =
            findByNames(tagNames = names.toSet { it.value }).associateBy { it.name }

        val ret = mutableListOf<TagEntity>()
        val newEntities = mutableListOf<TagEntity>()
        names.forEach { name ->
            val currentEntity = currentNameToEntityTable[name.value]
            if (currentEntity == null) {
                newEntities.add(TagEntity(name = name.value))
            } else {
                ret += currentEntity
            }
        }
        if (newEntities.isNotEmpty()) {
            val ids = insert(entities = newEntities)
            newEntities.zip(ids) { entity, id -> entity.copy(tagId = id) }.forEach { ret += it }
        }
        return ret
    }

    @Query("SELECT * FROM tag WHERE tag_id = :tagId")
    protected abstract suspend fun findById(tagId: Long): TagEntity?

    @Query("SELECT * FROM tag WHERE name = :tagName")
    abstract suspend fun findByName(tagName: String): TagEntity?

    @Query("SELECT * FROM tag WHERE name IN (:tagNames)")
    internal abstract suspend fun findByNames(tagNames: Set<String>): List<TagEntity>

    @Query("SELECT * FROM tag WHERE tag_id IN (:tagIds)")
    protected abstract fun findByIdsAsStreamInternal(tagIds: Set<Long>): Flow<List<TagEntity>>

    fun findByIdsAsStream(tagIds: Set<Long>): Flow<List<TagEntity>> =
        if (tagIds.isEmpty()) flowOf(emptyList())
        else findByIdsAsStreamInternal(tagIds)

    @Query("SELECT * FROM tag")
    abstract fun getAsStream(): Flow<List<TagEntity>>

    @Update
    protected abstract suspend fun update(entity: TagEntity)

    @Transaction
    open suspend fun updateAndGet(tagId: Long, name: NonBlankString): TagEntity {
        val oldEntity = checkNotNull(findById(tagId))
        val newEntity = TagEntity(tagId = tagId, name = name.value)
        if (oldEntity == newEntity) return oldEntity // No changes

        update(newEntity)
        return newEntity
    }

    @Query("DELETE FROM tag")
    abstract suspend fun deleteAll()

    @Query("DELETE FROM tag WHERE tag_id = :tagId")
    abstract suspend fun deleteById(tagId: Long)

    @Query("DELETE FROM tag WHERE tag_id NOT IN (:tagIds)")
    protected abstract suspend fun deleteByNotInIdsInternal(tagIds: Set<Long>)

    /**
     * Remove data not included in [tagIds].
     * If [tagIds] is empty, delete all data.
     *
     * @param tagIds tag ID set to preserve
     */
    suspend fun deleteByNotInIds(tagIds: Set<Long>) {
        if (tagIds.isEmpty()) deleteAll()
        else deleteByNotInIdsInternal(tagIds)
    }
}