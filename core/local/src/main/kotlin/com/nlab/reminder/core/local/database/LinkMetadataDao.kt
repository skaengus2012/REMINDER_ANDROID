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

import androidx.room.*

/**
 * @author thalys
 */
@Dao
abstract class LinkMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertInternal(linkMetadata: LinkMetadataEntity)

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
    protected abstract suspend fun deleteOldData(count: Int)

    @Query("SELECT count(*) FROM link_metadata")
    protected abstract suspend fun getCount(): Int

    @Transaction
    open suspend fun insert(linkMetadata: LinkMetadataEntity) {
        insertInternal(linkMetadata)

        val curCount: Int = getCount()
        if (curCount > MAX_CACHE_COUNT) {
            deleteOldData(count = MAX_CACHE_COUNT - curCount)
        }
    }

    @Query("SELECT * FROM link_metadata WHERE link = :link")
    abstract suspend fun findByLinks(link: List<String>): List<LinkMetadataEntity>

    companion object {
        private const val MAX_CACHE_COUNT = 100
    }
}