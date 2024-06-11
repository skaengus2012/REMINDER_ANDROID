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

import com.nlab.reminder.core.data.local.database.toModels
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlinx.coroutine.flow.map
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.catching
import com.nlab.reminder.core.local.database.ScheduleTagListDao
import com.nlab.reminder.core.local.database.TagDao
import com.nlab.reminder.core.local.database.TagEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author Doohyun
 */
class LocalTagRepository(
    private val tagDao: TagDao,
    private val scheduleTagListDao: ScheduleTagListDao
) : TagRepository {
    override fun getStream(): Flow<List<Tag>> = tagDao.getAsStream().map { it.toModels() }

    override suspend fun getUsageCount(id: TagId): Result<Long> = catching {
        scheduleTagListDao.findTagUsageCount(tagId = id.value)
    }

    override suspend fun updateName(id: TagId, name: String): Result<Unit> = catching {
        tagDao.update(TagEntity(tagId = id.value, name = name))
    }

    override suspend fun delete(id: TagId): Result<Unit> = catching {
        tagDao.deleteById(id.value)
    }
}