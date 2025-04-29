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
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.local.database.model.LinkMetadataDTO
import com.nlab.reminder.core.local.database.model.LinkMetadataEntity

private const val MAX_CACHE_COUNT = 1000

@Dao
abstract class LinkMetadataDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertOrReplace(entity: LinkMetadataEntity)

    @Query("SELECT link FROM link_metadata ORDER BY insertion_order")
    protected abstract suspend fun getAllSortedLinks(): List<String>

    @Query("SELECT count(*) FROM link_metadata")
    protected abstract suspend fun getTotalCount(): Int

    @Query("SELECT * FROM link_metadata WHERE link IN (:links)")
    protected abstract suspend fun findByLinksInternal(links: Set<String>): List<LinkMetadataEntity>

    suspend fun findByLinks(links: Set<NonBlankString>): List<LinkMetadataEntity> =
        if (links.isEmpty()) emptyList()
        else findByLinksInternal(links.toSet { it.value })

    @Query("UPDATE link_metadata SET insertion_order = :insertionOrder WHERE link = :link")
    protected abstract suspend fun updateInsertionOrder(link: String, insertionOrder: Int)

    @Query(
        """
        DELETE 
        FROM link_metadata
        WHERE link IN(
            SELECT link
            FROM link_metadata
            ORDER BY insertion_order
            LIMIT :count
        )
        """
    )
    protected abstract suspend fun deleteOldestBy(count: Int)

    @Transaction
    open suspend fun insertAndGet(metadataDTO: LinkMetadataDTO): LinkMetadataEntity {
        // insert
        val newEntity = LinkMetadataEntity(
            link = metadataDTO.link.value,
            title = metadataDTO.title?.value,
            imageUrl = metadataDTO.imageUrl?.value,
            insertionOrder = Int.MAX_VALUE
        )
        insertOrReplace(newEntity)

        // remove oldest entities
        val curCount: Int = getTotalCount()
        if (curCount > MAX_CACHE_COUNT) {
            deleteOldestBy(count = curCount - MAX_CACHE_COUNT)
        }

        // re-order insertion order
        getAllSortedLinks().forEachIndexed { index, link ->
            updateInsertionOrder(link = link, insertionOrder = index + 1)
        }

        return newEntity
    }
}