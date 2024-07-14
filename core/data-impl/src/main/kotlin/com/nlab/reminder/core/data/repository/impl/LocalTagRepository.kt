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

package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.data.local.database.toModel
import com.nlab.reminder.core.data.local.database.toModels
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.data.repository.TagGetQuery
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlinx.coroutine.flow.map
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.annotation.Generated
import com.nlab.reminder.core.kotlin.catching
import com.nlab.reminder.core.kotlin.map
import com.nlab.reminder.core.local.database.ScheduleTagListDao
import com.nlab.reminder.core.local.database.TagDao
import com.nlab.reminder.core.local.database.TagEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * @author Doohyun
 */
class LocalTagRepository(
    private val tagDao: TagDao,
    private val scheduleTagListDao: ScheduleTagListDao
) : TagRepository {
    override suspend fun save(tag: Tag): Result<Tag> = catching {
        val savedTagId = saveAndGetTagId(tag)
        val curTag = tagDao.findByIds(tagIds = listOf(savedTagId))
            .firstOrNull()
            ?: throw IllegalStateException("Tag was empty")
        return@catching curTag.toModel()
    }

    private suspend fun saveAndGetTagId(tag: Tag): Long = when (val tagId = tag.id) {
        is TagId.Empty -> tagDao.insert(TagEntity(name = tag.name))
        is TagId.Present -> tagId.value.also { tagDao.update(TagEntity(tagId = it, name = tag.name)) }
    }

    override suspend fun delete(id: TagId) = catching {
        require(id is TagId.Present)
        tagDao.deleteById(id.value)
    }

    override suspend fun getUsageCount(id: TagId): Result<Long> = catching {
        require(id is TagId.Present)
        scheduleTagListDao.findTagUsageCount(tagId = id.value)
    }

    override suspend fun getTags(query: TagGetQuery): Result<List<Tag>> {
        val tagEntities = when (query) {
            // When outside the catch block, jacoco does not recognize. 😭
            is TagGetQuery.All -> catching { tagDao.get() }
            is TagGetQuery.ByIds -> catching {
                query.tagIds.mapTo(
                    transform = { ids -> tagDao.findByIds(ids) },
                    onEmpty = { emptyList() }
                )
            }
        }
        return tagEntities.map { it.toModels() }
    }

    override fun getTagsAsStream(query: TagGetQuery): Flow<List<Tag>> {
        val entitiesFlow: Flow<List<TagEntity>> = when (query) {
            is TagGetQuery.All -> tagDao.getAsStream()
            is TagGetQuery.ByIds -> query.tagIds.mapTo(
                transform = { ids -> tagDao.findByIdsAsStream(ids) },
                onEmpty = { emptyFlow() }
            )
        }
        return entitiesFlow.map { it.toModels() }
    }
}

@Generated
private inline fun <T> List<TagId>.mapTo(
    transform: (List<Long>) -> T,
    onEmpty: () -> T,
): T {
    require(all { it is TagId.Present })
    val ids = map { (it as TagId.Present).value }.distinct()
    return if (ids.isEmpty()) onEmpty() else transform(ids)
}