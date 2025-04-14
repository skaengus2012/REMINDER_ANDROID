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
import com.nlab.reminder.core.data.model.TagUsage
import com.nlab.reminder.core.data.model.TagUsages
import com.nlab.reminder.core.data.repository.GetTagQuery
import com.nlab.reminder.core.data.repository.GetTagUsageQuery
import com.nlab.reminder.core.data.repository.SaveTagQuery
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlinx.coroutine.flow.map
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.catching
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.map
import com.nlab.reminder.core.kotlin.trim
import com.nlab.reminder.core.kotlinx.coroutine.flow.flatMapLatest
import com.nlab.reminder.core.local.database.dao.ScheduleTagListDAO
import com.nlab.reminder.core.local.database.dao.TagDAO
import com.nlab.reminder.core.local.database.model.TagEntity
import com.nlab.reminder.core.local.database.transaction.UpdateOrReplaceAndGetTagTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * @author Doohyun
 */
class LocalTagRepository(
    private val tagDAO: TagDAO,
    private val scheduleTagListDAO: ScheduleTagListDAO,
    private val updateOrReplaceAndGetTag: UpdateOrReplaceAndGetTagTransaction
) : TagRepository {
    override suspend fun save(query: SaveTagQuery): Result<Tag> {
        val entityResult = catching {
            when (query) {
                is SaveTagQuery.Add -> {
                    tagDAO.insertAndGet(name = query.name.trim())
                }

                is SaveTagQuery.Modify -> {
                    updateOrReplaceAndGetTag(tagId = query.id.rawId, name = query.name.trim())
                }
            }
        }
        return entityResult.map(::Tag)
    }

    override suspend fun delete(id: TagId) = catching {
        tagDAO.deleteById(id.rawId)
    }

    override fun getTagsAsStream(query: GetTagQuery): Flow<Set<Tag>> {
        val entitiesFlow: Flow<Array<TagEntity>> = when (query) {
            is GetTagQuery.ByIds -> {
                tagDAO.findByIdsAsStream(query.tagIds.toSet(TagId::rawId))
            }
        }
        return entitiesFlow.distinctUntilChanged().map { entities -> entities.toSet(::Tag) }
    }

    override fun getTagUsagesAsStream(query: GetTagUsageQuery): Flow<Set<TagUsage>> {
        val tagEntitiesFlow = when (query) {
            is GetTagUsageQuery.All -> tagDAO.getAsStream()
        }
        return tagEntitiesFlow.distinctUntilChanged().flatMapLatest { tagEntities ->
            scheduleTagListDAO
                .findByTagIdsAsStream(tagIds = tagEntities.toSet(TagEntity::tagId))
                .distinctUntilChanged()
                .map { scheduleTagListEntities -> TagUsages(tagEntities, scheduleTagListEntities) }
        }
    }
}