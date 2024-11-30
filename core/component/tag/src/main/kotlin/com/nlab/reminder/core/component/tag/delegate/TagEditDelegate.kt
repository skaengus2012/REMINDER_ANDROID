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

import com.nlab.reminder.core.component.tag.model.TagEditStep
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.repository.SaveTagQuery
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.domain.TagGroupSource
import com.nlab.reminder.core.domain.TryUpdateTagNameResult
import com.nlab.reminder.core.domain.TryUpdateTagNameUseCase
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.isFailure
import com.nlab.reminder.core.kotlin.map
import com.nlab.reminder.core.kotlin.tryToNonBlankStringOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * @author Doohyun
 */
class TagEditDelegate @Inject constructor(
    private val tagRepository: TagRepository,
    private val tryUpdateTagNameUseCase: TryUpdateTagNameUseCase,
) {
    suspend fun startEditing(tag: Tag): Result<TagEditStep.Intro> =
        tagRepository.getUsageCount(id = tag.id)
            .map { usageCount -> TagEditStep.Intro(tag, usageCount) }

    fun startRename(current: TagEditStep): TagEditStep =
        if (current !is TagEditStep.Intro) current
        else TagEditStep.Rename(
            tag = current.tag,
            usageCount = current.usageCount,
            renameText = current.tag.name.value,
            shouldUserInputReady = true,
        )

    fun readyRenameInput(current: TagEditStep): TagEditStep =
        if (current !is TagEditStep.Rename) current
        else current.copy(shouldUserInputReady = false)

    fun changeRenameText(current: TagEditStep, input: String): TagEditStep =
        if (current !is TagEditStep.Rename) current
        else current.copy(renameText = input)

    fun tryUpdateTagName(
        current: TagEditStep,
        loadedTagsSnapshot: List<Tag>
    ): Flow<LazyTagEditResult> = flow {
        if (current !is TagEditStep.Rename) return@flow
        emit(FinishTagEditResult(current))

        val newName = current.renameText.tryToNonBlankStringOrNull() ?: return@flow
        val tryUpdateTagNameResult = tryUpdateTagNameUseCase.invoke(
            tagId = current.tag.id,
            newName = newName,
            tagGroup = TagGroupSource.Snapshot(loadedTagsSnapshot)
        )
        when (tryUpdateTagNameResult) {
            is TryUpdateTagNameResult.DuplicateNameError -> {
                val merge = TagEditStep.Merge(
                    from = current.tag,
                    to = tryUpdateTagNameResult.duplicateTag,
                )

                emit(LazyTagEditResult.ToNextStep { current -> current ?: merge })
            }

            is TryUpdateTagNameResult.UnknownError -> {
                emit(LazyTagEditResult.UnknownError)
            }

            else -> Unit
        }
    }

    fun mergeTag(current: TagEditStep): Flow<LazyTagEditResult> = flow {
        if (current !is TagEditStep.Merge) return@flow
        emit(FinishTagEditResult(current))

        val query = SaveTagQuery.Modify(id = current.from.id, name = current.to.name)
        val result = tagRepository.save(query)
        if (result.isFailure) {
            emit(LazyTagEditResult.UnknownError)
        }
    }

    fun startDelete(current: TagEditStep): TagEditStep =
        if (current !is TagEditStep.Intro) current
        else TagEditStep.Delete(
            tag = current.tag,
            usageCount = current.usageCount
        )

    fun deleteTag(current: TagEditStep): Flow<LazyTagEditResult> = flow {
        if (current !is TagEditStep.Delete) return@flow
        emit(FinishTagEditResult(current))

        val result = tagRepository.delete(current.tag.id)
        if (result.isFailure) {
            emit(LazyTagEditResult.UnknownError)
        }
    }
}

sealed interface LazyTagEditResult {
    fun interface ToNextStep : LazyTagEditResult {
        operator fun invoke(currentStep: TagEditStep?): TagEditStep?
    }

    data object UnknownError : LazyTagEditResult
}

@Suppress("FunctionName")
private fun FinishTagEditResult(compare: TagEditStep) = LazyTagEditResult.ToNextStep { currentStep ->
    if (currentStep == compare) null
    else currentStep
}