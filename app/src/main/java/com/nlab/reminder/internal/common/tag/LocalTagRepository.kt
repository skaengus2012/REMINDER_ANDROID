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

package com.nlab.reminder.internal.common.tag

import com.nlab.reminder.core.kotlin.flow.map
import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.tag.TagRepository
import com.nlab.reminder.domain.common.util.DatabaseQualifier
import com.nlab.reminder.internal.common.android.database.TagDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * @author Doohyun
 */
class LocalTagRepository @Inject constructor(
    private val tagDao: TagDao,
    @DatabaseQualifier private val dispatcher: CoroutineDispatcher
) : TagRepository {
    override fun get(): Flow<List<Tag>> =
        tagDao.find()
            .map { tagEntities -> tagEntities.map { Tag(it.tagId, it.name) } }
            .flowOn(dispatcher)
}