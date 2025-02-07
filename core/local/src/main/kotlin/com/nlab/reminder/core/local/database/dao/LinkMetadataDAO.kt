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
import com.nlab.reminder.core.local.database.model.LinkMetadataEntity

private const val MAX_CACHE_COUNT = 1000

@Dao
abstract class LinkMetadataDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertOrReplace(entity: LinkMetadataEntity)

    @Query("SELECT insertion_order FROM link_metadata ORDER BY insertion_order DESC LIMIT 1")
    protected abstract suspend fun getMaxInsertionOrder(): Long?

    @Query("SELECT count(*) FROM link_metadata")
    protected abstract suspend fun getTotalCount(): Int

    @Query("SELECT * FROM link_metadata WHERE link IN (:links)")
    protected abstract suspend fun findByLinksInternal(links: Set<String>): Array<LinkMetadataEntity>

    suspend fun findByLinks(links: Set<NonBlankString>): Array<LinkMetadataEntity> =
        if (links.isEmpty()) emptyArray()
        else findByLinksInternal(links.toSet { it.value })

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
        val newEntity = LinkMetadataEntity(
            link = metadataDTO.link.value,
            title = metadataDTO.title?.value,
            imageUrl = metadataDTO.imageUrl?.value,
            insertionOrder = (getMaxInsertionOrder() ?: -1) + 1
        )
        insertOrReplace(newEntity)

        // remove oldest entities
        val curCount: Int = getTotalCount()
        if (curCount > MAX_CACHE_COUNT) {
            deleteOldestBy(count = curCount - MAX_CACHE_COUNT)
        }

        return newEntity
    }
}

data class LinkMetadataDTO(
    val link: NonBlankString,
    val title: NonBlankString?,
    val imageUrl: NonBlankString?
)