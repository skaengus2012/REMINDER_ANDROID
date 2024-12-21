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
class TryUpdateTagNameUseCase(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(
        tagId: TagId,
        newName: NonBlankString,
        tagGroup: TagGroupSource
    ): TryUpdateTagNameResult {
        val compareTags = when (tagGroup) {
            is TagGroupSource.Snapshot -> tagGroup.tags
        }
        val sameNameTags = compareTags.filter { it.name == newName }
        return when {
            sameNameTags.isEmpty() -> {
                tagRepository.save(SaveTagQuery.Modify(tagId, newName))
                    .map { TryUpdateTagNameResult.Success(it) }
                    .getOrElse { TryUpdateTagNameResult.UnknownError }
            }
            sameNameTags.any { it.id == tagId } -> TryUpdateTagNameResult.NotChanged
            else -> TryUpdateTagNameResult.DuplicateNameError(duplicateTag = sameNameTags.first())
        }
    }
}

sealed class TagGroupSource private constructor() {
    data class Snapshot(val tags: List<Tag>) : TagGroupSource()
}

sealed class TryUpdateTagNameResult private constructor() {
    data class Success(val tag: Tag) : TryUpdateTagNameResult()
    data object NotChanged : TryUpdateTagNameResult()
    data class DuplicateNameError(val duplicateTag: Tag) : TryUpdateTagNameResult()
    data object UnknownError : TryUpdateTagNameResult()
}