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

package com.nlab.reminder.core.component.tag.delegate

import com.nlab.reminder.core.component.tag.model.TagEditInteraction
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.domain.TagGroupSource
import com.nlab.reminder.core.domain.TryUpdateTagNameResult
import com.nlab.reminder.core.domain.TryUpdateTagNameUseCase
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.map
import com.nlab.reminder.core.kotlin.tryToNonBlankStringOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * @author Doohyun
 */
class TagEditDelegate(
    private val tagRepository: TagRepository,
    private val tryUpdateTagNameUseCase: TryUpdateTagNameUseCase
) {
    private val _interaction = MutableStateFlow<TagEditInteraction>(value = TagEditInteraction.Empty)
    val interaction: StateFlow<TagEditInteraction> = _interaction.asStateFlow()

    suspend fun startEditing(tag: Tag): Result<Unit> =
        tagRepository.getUsageCount(id = tag.id)
            .map { usageCount ->
                val intro = TagEditInteraction.Intro(tag, usageCount)
                _interaction.update { current ->
                    if (current is TagEditInteraction.Empty) intro
                    else current
                }
            }

    fun startRename() {
        _interaction.update { current ->
            if (current !is TagEditInteraction.Intro) current
            else TagEditInteraction.Rename(
                tag = current.tag,
                usageCount = current.usageCount,
                renameText = current.tag.name.value,
                shouldUserInputReady = true
            )
        }
    }

    fun readyRenameInput() {
        _interaction.update { current ->
            if (current !is TagEditInteraction.Rename) current
            else current.copy(shouldUserInputReady = false)
        }
    }

    fun changeRenameText(input: String) {
        _interaction.update { current ->
            if (current !is TagEditInteraction.Rename) current
            else current.copy(renameText = input)
        }
    }

    suspend fun tryUpdateTagName(loadedTagsSnapshot: List<Tag>) {
        val curRenameInteraction = _interaction.value as? TagEditInteraction.Rename ?: return
        val result = tryUpdateTagNameUseCase.invoke(
            tagId = curRenameInteraction.tag.id,
            newName = curRenameInteraction.renameText.tryToNonBlankStringOrNull() ?: return,
            tagGroup = TagGroupSource.Snapshot(loadedTagsSnapshot)
        )
        when (result) {
            is TryUpdateTagNameResult.Success,
            is TryUpdateTagNameResult.NotChanged -> {
                _interaction.update { current ->
                    if (current == curRenameInteraction) TagEditInteraction.Empty
                    else current
                }
            }
            is TryUpdateTagNameResult.DuplicateNameError -> {
                val merge = TagEditInteraction.Merge(from = curRenameInteraction.tag, to = result.duplicateTag)
                _interaction.update { current ->
                    if (current == curRenameInteraction) merge
                    else current
                }
            }
            is TryUpdateTagNameResult.UnknownError -> {

            }
        }
    }
}