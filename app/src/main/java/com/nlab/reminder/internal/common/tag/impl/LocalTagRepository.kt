/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.internal.common.tag.impl

import com.nlab.reminder.core.kotlin.coroutine.flow.map
import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.core.kotlin.util.catching
import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.tag.TagRepository
import com.nlab.reminder.internal.common.android.database.*
import kotlinx.coroutines.flow.Flow

/**
 * @author Doohyun
 */
class LocalTagRepository(
    private val tagDao: TagDao,
    private val scheduleTagListDao: ScheduleTagListDao
) : TagRepository {
    override fun get(): Flow<List<Tag>> = tagDao.find().map { it.toTags() }

    override suspend fun getUsageCount(tag: Tag): Result<Long> = catching {
        scheduleTagListDao.findTagUsageCount(tagId = tag.tagId)
    }

    override suspend fun updateName(tag: Tag, name: String): Result<Unit> = catching {
        tagDao.update(tag.toEntity(name = name))
    }

    override suspend fun delete(tag: Tag): Result<Unit> = catching {
        tagDao.delete(tag.toEntity())
    }
}