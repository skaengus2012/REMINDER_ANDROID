/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.core.data.repository

import kotlinx.coroutines.flow.Flow
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.kotlin.annotation.Generated

/**
 * @author Doohyun
 */
interface TagRepository {
    suspend fun save(tag: Tag): Result<Tag>
    suspend fun delete(id: TagId): Result<Unit>
    suspend fun getTags(query: TagGetQuery): Result<List<Tag>>
    fun getStream(): Flow<List<Tag>>

    suspend fun getUsageCount(id: TagId): Result<Long>
}

sealed class TagGetQuery private constructor() {
    @Generated
    data object All : TagGetQuery()

    @Generated
    data class ByIds(val tagIds: List<TagId>) : TagGetQuery()
}