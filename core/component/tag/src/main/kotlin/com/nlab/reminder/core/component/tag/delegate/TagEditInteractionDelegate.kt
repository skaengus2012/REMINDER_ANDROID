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
import kotlin.reflect.KClass

/**
 * @author Doohyun
 */
class TagEditInteractionDelegate @Inject constructor(
    private val tagRepository: TagRepository,
    private val tryUpdateTagNameUseCase: TryUpdateTagNameUseCase,
) {
    suspend fun startEditing(tag: Tag): Result<TagEditInteraction> =
        tagRepository.getUsageCount(id = tag.id)
            .map { usageCount -> TagEditInteraction.Intro(tag, usageCount) }

    fun startRename(current: TagEditInteraction): TagEditInteraction =
        if (current !is TagEditInteraction.Intro) current
        else TagEditInteraction.Rename(
            tag = current.tag,
            usageCount = current.usageCount,
            renameText = current.tag.name.value,
            shouldUserInputReady = true,
        )

    fun readyRenameInput(current: TagEditInteraction): TagEditInteraction =
        if (current !is TagEditInteraction.Rename) current
        else current.copy(shouldUserInputReady = false)

    fun changeRenameText(current: TagEditInteraction, input: String): TagEditInteraction =
        if (current !is TagEditInteraction.Rename) current
        else current.copy(renameText = input)

    fun tryUpdateTagName(
        current: TagEditInteraction,
        loadedTagsSnapshot: List<Tag>
    ): Flow<LazyTagEditInteractionResult> = flow {
        emit(FinishTagEditInteractionResult<TagEditInteraction.Rename>(current))
        val curInteraction = current as? TagEditInteraction.Rename ?: return@flow
        val newName = curInteraction.renameText.tryToNonBlankStringOrNull() ?: return@flow
        val tryUpdateTagNameResult = tryUpdateTagNameUseCase.invoke(
            tagId = curInteraction.tag.id,
            newName = newName,
            tagGroup = TagGroupSource.Snapshot(loadedTagsSnapshot)
        )
        when (tryUpdateTagNameResult) {
            is TryUpdateTagNameResult.DuplicateNameError -> {
                val merge = TagEditInteraction.Merge(
                    from = curInteraction.tag,
                    to = tryUpdateTagNameResult.duplicateTag,
                )

                emit(LazyTagEditInteractionResult.NextInteraction { current -> current ?: merge })
            }

            is TryUpdateTagNameResult.UnknownError -> {
                emit(LazyTagEditInteractionResult.UnknownError)
            }

            else -> Unit
        }
    }

    fun mergeTag(current: TagEditInteraction): Flow<LazyTagEditInteractionResult> = flow {
        emit(FinishTagEditInteractionResult<TagEditInteraction.Merge>(current))
        val curInteraction = current as? TagEditInteraction.Merge ?: return@flow
        val query = SaveTagQuery.Modify(id = curInteraction.from.id, name = curInteraction.to.name)
        val result = tagRepository.save(query)
        if (result.isFailure) {
            emit(LazyTagEditInteractionResult.UnknownError)
        }
    }

    fun startDelete(current: TagEditInteraction): TagEditInteraction =
        if (current !is TagEditInteraction.Intro) current
        else TagEditInteraction.Delete(
            tag = current.tag,
            usageCount = current.usageCount
        )

    fun deleteTag(current: TagEditInteraction): Flow<LazyTagEditInteractionResult> = flow {
        emit(FinishTagEditInteractionResult<TagEditInteraction.Delete>(current))
        val curInteraction = current as? TagEditInteraction.Delete ?: return@flow
        val result = tagRepository.delete(curInteraction.tag.id)
        if (result.isFailure) {
            emit(LazyTagEditInteractionResult.UnknownError)
        }
    }
}

sealed interface LazyTagEditInteractionResult {
    fun interface NextInteraction : LazyTagEditInteractionResult {
        operator fun invoke(interaction: TagEditInteraction?): TagEditInteraction?
    }

    data object UnknownError : LazyTagEditInteractionResult
}

@Suppress("FunctionName")
private inline fun <reified T : TagEditInteraction> FinishTagEditInteractionResult(compare: TagEditInteraction) =
    FinishTagEditInteractionResult(compare, T::class)

@Suppress("FunctionName")
private fun <T : TagEditInteraction> FinishTagEditInteractionResult(compare: TagEditInteraction, type: KClass<T>) =
    LazyTagEditInteractionResult.NextInteraction { current ->
        if (type.isInstance(current) && current == compare) null
        else current
    }