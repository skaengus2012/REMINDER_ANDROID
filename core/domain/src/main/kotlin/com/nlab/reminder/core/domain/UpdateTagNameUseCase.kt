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

package com.nlab.reminder.core.domain

import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.data.repository.SaveTagQuery
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.getOrElse
import com.nlab.reminder.core.kotlin.map

/**
 * @author Thalys
 */
class UpdateTagNameUseCase(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(
        tagId: TagId,
        newName: NonBlankString,
        tagGroup: TagGroupSource
    ): UpdateTagNameResult {
        val compareTags = when (tagGroup) {
            is TagGroupSource.Snapshot -> tagGroup.tags
        }
        val sameNameTagIds = compareTags
            .asSequence()
            .filter { it.name == newName }
            .map { it.id }
            .toSet()
        return when {
            sameNameTagIds.isEmpty() -> {
                tagRepository.save(SaveTagQuery.Modify(tagId, newName))
                    .map { UpdateTagNameResult.Success(it) }
                    .getOrElse { UpdateTagNameResult.UnknownError }
            }
            tagId in sameNameTagIds -> UpdateTagNameResult.NotChanged
            else -> UpdateTagNameResult.DuplicateNameError
        }
    }
}

sealed class TagGroupSource private constructor() {
    data class Snapshot(val tags: List<Tag>) : TagGroupSource()
}

sealed class UpdateTagNameResult private constructor() {
    data class Success(val tag: Tag) : UpdateTagNameResult()
    data object NotChanged : UpdateTagNameResult()
    data object DuplicateNameError : UpdateTagNameResult()
    data object UnknownError : UpdateTagNameResult()
}