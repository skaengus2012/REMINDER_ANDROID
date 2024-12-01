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

package com.nlab.reminder.core.component.tag.edit

import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.repository.SaveTagQuery
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.domain.TagGroupSource
import com.nlab.reminder.core.domain.TryUpdateTagNameResult
import com.nlab.reminder.core.domain.TryUpdateTagNameUseCase
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.map
import com.nlab.reminder.core.kotlin.onSuccess
import com.nlab.reminder.core.kotlin.tryToNonBlankStringOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update

/**
 * @author Doohyun
 */
class TagEditDelegate(
    initialStep: TagEditStep,
    private val tagRepository: TagRepository,
    private val tryUpdateTagNameUseCase: TryUpdateTagNameUseCase,
) {
    private val _step = MutableStateFlow(initialStep)
    val step: StateFlow<TagEditStep> = _step.asStateFlow()

    suspend fun startEditing(tag: Tag): Result<TagEditStep.Intro> =
        tagRepository.getUsageCount(id = tag.id)
            .map { usageCount -> TagEditStep.Intro(tag, usageCount) }
            .onSuccess { intro -> _step.updateIfTypeOf<TagEditStep.Empty> { intro } }

    fun startRename() {
        _step.updateIfTypeOf<TagEditStep.Intro> { current ->
            TagEditStep.Rename(
                tag = current.tag,
                usageCount = current.usageCount,
                renameText = current.tag.name.value,
                shouldUserInputReady = true,
            )
        }
    }

    fun readyRenameInput() {
        _step.updateIfTypeOf<TagEditStep.Rename> { current ->
            current.copy(shouldUserInputReady = false)
        }
    }

    fun changeRenameText(input: String) {
        _step.updateIfTypeOf<TagEditStep.Rename> { current ->
            current.copy(renameText = input)
        }
    }

    suspend fun tryUpdateTagName(loadedTagsSnapshot: List<Tag>): Result<Unit> {
        val emitStep = _step.getAndUpdateToEmptyIfTypeOf<TagEditStep.Rename>()
        if (emitStep !is TagEditStep.Rename) return Result.Success(Unit)

        val targetTag = emitStep.tag
        val newName = emitStep.renameText.tryToNonBlankStringOrNull() ?: return Result.Success(Unit)

        val tryUpdateTagNameResult = tryUpdateTagNameUseCase.invoke(
            tagId = targetTag.id,
            newName = newName,
            tagGroup = TagGroupSource.Snapshot(loadedTagsSnapshot)
        )
        return when (tryUpdateTagNameResult) {
            is TryUpdateTagNameResult.Success,
            is TryUpdateTagNameResult.NotChanged ->  Result.Success(Unit)

            is TryUpdateTagNameResult.DuplicateNameError -> {
                val merge = TagEditStep.Merge(
                    from = targetTag,
                    to = tryUpdateTagNameResult.duplicateTag,
                )
                _step.updateIfTypeOf<TagEditStep.Empty> { merge }

                Result.Success(Unit)
            }

            is TryUpdateTagNameResult.UnknownError -> Result.Failure(IllegalStateException())
        }
    }

    suspend fun mergeTag(): Result<Unit> {
        val emitStep = _step.getAndUpdateToEmptyIfTypeOf<TagEditStep.Merge>()
        if (emitStep !is TagEditStep.Merge) return Result.Success(Unit)

        val query = SaveTagQuery.Modify(id = emitStep.from.id, name = emitStep.to.name)
        return tagRepository.save(query).map {}
    }

    fun startDelete() {
        _step.updateIfTypeOf<TagEditStep.Intro> { current ->
            TagEditStep.Delete(
                tag = current.tag,
                usageCount = current.usageCount
            )
        }
    }

    suspend fun deleteTag(): Result<Unit> {
        val emitStep = _step.getAndUpdateToEmptyIfTypeOf<TagEditStep.Delete>()
        if (emitStep !is TagEditStep.Delete) return Result.Success(Unit)

        return tagRepository.delete(emitStep.tag.id).map {}
    }

    fun clearStep() {
        _step.update { TagEditStep.Empty }
    }
}

private inline fun <reified T : TagEditStep> MutableStateFlow<TagEditStep>.updateIfTypeOf(block: (T) -> TagEditStep) {
    update { current ->
        if (current is T) block(current)
        else current
    }
}

private inline fun <reified T : TagEditStep>  MutableStateFlow<TagEditStep>.getAndUpdateToEmptyIfTypeOf(): TagEditStep {
    return getAndUpdate { current ->
        if (current is T) TagEditStep.Empty
        else current
    }
}