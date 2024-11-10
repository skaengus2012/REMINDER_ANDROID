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
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.nlab.reminder.core.local.database.model.LinkMetadataEntity

private const val MAX_CACHE_COUNT = 100

@Dao
abstract class LinkMetadataDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertInternal(entity: LinkMetadataEntity)

    @Query("SELECT count(*) FROM link_metadata")
    protected abstract suspend fun getTotalCount(): Int

    @Query("SELECT * FROM link_metadata WHERE link IN (:links)")
    protected abstract suspend fun findByLinksInternal(links: Set<String>): Array<LinkMetadataEntity>

    suspend fun findByLinks(links: Set<String>): Array<LinkMetadataEntity> =
        if (links.isEmpty()) emptyArray()
        else findByLinksInternal(links)

    @Query(
        """
        DELETE 
        FROM link_metadata
        WHERE link IN(
            SELECT link
            FROM link_metadata
            ORDER BY timestamp
            LIMIT :count
        )
        """
    )
    protected abstract suspend fun deleteOldestBy(count: Int)

    @Transaction
    open suspend fun insert(entity: LinkMetadataEntity) {
        insertInternal(entity)

        val curCount: Int = getTotalCount()
        if (curCount > MAX_CACHE_COUNT) {
            deleteOldestBy(count = MAX_CACHE_COUNT - curCount)
        }
    }
}