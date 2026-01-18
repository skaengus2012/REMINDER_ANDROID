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

import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.data.repository.GetTagQuery
import com.nlab.reminder.core.data.repository.SaveBulkTagQuery
import com.nlab.reminder.core.data.repository.SaveTagQuery
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.NonNegativeInt
import com.nlab.reminder.core.kotlinx.coroutines.flow.map
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.catching
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.map
import com.nlab.reminder.core.kotlin.toNonNegativeInt
import com.nlab.reminder.core.kotlin.trim
import com.nlab.reminder.core.kotlinx.coroutines.flow.combine
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.dao.TagDAO
import com.nlab.reminder.core.local.database.entity.TagEntity
import com.nlab.reminder.core.local.database.transaction.UpdateOrMergeAndGetTagTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * @author Doohyun
 */
class LocalTagRepository(
    private val tagDAO: TagDAO,
    private val scheduleTagListDAO: ScheduleTagListDAO,
    private val updateOrMergeAndGetTag: UpdateOrMergeAndGetTagTransaction
) : TagRepository {
    override suspend fun save(query: SaveTagQuery): Result<Tag> {
        val entityResult = catching {
            when (query) {
                is SaveTagQuery.Add -> {
                    tagDAO.insertAndGet(name = query.name.trim())
                }

                is SaveTagQuery.Modify -> {
                    val rawTagId = query.id.rawId
                    val trimmedName = query.name.trim()
                    if (query.shouldMergeIfExists) updateOrMergeAndGetTag(tagId = rawTagId, name = trimmedName)
                    else tagDAO.updateAndGet(rawTagId, trimmedName)
                }
            }
        }
        return entityResult.map(::Tag)
    }

    override suspend fun saveBulk(query: SaveBulkTagQuery): Result<Set<Tag>> {
        val entitiesResult = catching {
            when (query) {
                is SaveBulkTagQuery.Add -> {
                    tagDAO.insertAndGet(names = query.names.toSet { it.trim() })
                }
            }
        }
        return entitiesResult.map { it.toTags() }
    }

    override suspend fun delete(id: TagId) = catching {
        tagDAO.deleteById(id.rawId)
    }

    override suspend fun getUsageCount(tagId: TagId): Result<NonNegativeInt> = catching {
        scheduleTagListDAO
            .findScheduleIdCountByTagId(tagId = tagId.rawId)
            .toNonNegativeInt()
    }

    override fun getTagsAsStream(query: GetTagQuery): Flow<Set<Tag>> {
        val entitiesFlow: Flow<List<TagEntity>> = when (query) {
            is GetTagQuery.OnlyUsed -> {
                combine(
                    tagDAO.getAsStream().distinctUntilChanged(),
                    scheduleTagListDAO.getAllTagIdsAsStream().distinctUntilChanged(),
                    transform = { tagEntities, usedTagIds ->
                        val usedTagIdsSet = usedTagIds.toSet()
                        tagEntities.filter { it.tagId in usedTagIdsSet }
                    }
                ).distinctUntilChanged()
            }

            is GetTagQuery.ByIds -> {
                tagDAO.findByIdsAsStream(query.tagIds.toSet(TagId::rawId)).distinctUntilChanged()
            }
        }
        return entitiesFlow.map { it.toTags() }
    }
}

private fun List<TagEntity>.toTags(): Set<Tag> = toSet(::Tag)