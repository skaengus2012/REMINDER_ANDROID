/*
 * Copyright (C) 2026 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.schedulelist.content

import com.nlab.reminder.core.data.model.ScheduleContent
import com.nlab.reminder.core.data.model.ScheduleId
import com.nlab.reminder.core.data.repository.SaveBulkTagQuery
import com.nlab.reminder.core.data.repository.SaveScheduleQuery
import com.nlab.reminder.core.data.repository.ScheduleRepository
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.getOrThrow

/**
 * @author Thalys
 */
class EditScheduleContentInListUseCase(
    private val scheduleRepository: ScheduleRepository,
    private val tagRepository: TagRepository,
) {
    suspend operator fun invoke(
        id: ScheduleId,
        originContent: ScheduleContent,
        title: NonBlankString,
        note: NonBlankString?,
        tagNames: Set<NonBlankString>,
    ): Result<Unit> = runCatching {
        val tagIds = tagRepository.saveBulk(query = SaveBulkTagQuery.Add(tagNames))
            .getOrThrow()
            .toSet { it.id }
        val newContent = originContent.copy(
            title = title,
            note = note,
            tagIds = tagIds
        )
        scheduleRepository
            .save(query = SaveScheduleQuery.Modify(id = id, content = newContent))
            .getOrThrow()
    }
}