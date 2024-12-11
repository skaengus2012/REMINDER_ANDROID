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
import kotlinx.coroutines.flow.update

/**
 * @author Doohyun
 */
class TagEditDelegate(
    initialState: TagEditState?,
    private val tagRepository: TagRepository,
    private val tryUpdateTagNameUseCase: TryUpdateTagNameUseCase,
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<TagEditState?> = _state.asStateFlow()

    suspend fun startEditing(tag: Tag): Result<TagEditState.Intro> =
        tagRepository.getUsageCount(id = tag.id)
            .map { usageCount -> TagEditState.Intro(tag, usageCount) }
            .onSuccess { intro -> _state.update { current -> current ?: intro } }

    fun startRename() {
        _state.updateIfTypeOf<TagEditState.Intro> { current ->
            TagEditState.Rename(
                tag = current.tag,
                usageCount = current.usageCount,
                renameText = current.tag.name.value,
                shouldUserInputReady = true
            )
        }
    }

    fun readyRenameInput() {
        _state.updateIfTypeOf<TagEditState.Rename> { current -> current.copy(shouldUserInputReady = false) }
    }

    fun changeRenameText(input: String) {
        _state.updateIfTypeOf<TagEditState.Rename> { current -> current.copy(renameText = input) }
    }

    suspend fun tryUpdateTagName(loadedTagsSnapshot: List<Tag>): Result<Unit> {
        return _state.processingScope<TagEditState.Rename, TryUpdateTagNameResult>(
            request = { emitState ->
                val newName = emitState.renameText.tryToNonBlankStringOrNull()
                if (newName == null) TryUpdateTagNameResult.NotChanged
                else tryUpdateTagNameUseCase.invoke(
                    tagId = emitState.tag.id,
                    newName = newName,
                    tagGroup = TagGroupSource.Snapshot(loadedTagsSnapshot)
                )
            },
            onFinished = { emitState, result ->
                val duplicateTag: Tag?
                val isSuccessReturn: Boolean
                when (result) {
                    is TryUpdateTagNameResult.Success,
                    is TryUpdateTagNameResult.NotChanged -> {
                        duplicateTag = null
                        isSuccessReturn = true
                    }

                    is TryUpdateTagNameResult.DuplicateNameError -> {
                        duplicateTag = result.duplicateTag
                        isSuccessReturn = true
                    }

                    is TryUpdateTagNameResult.UnknownError -> {
                        duplicateTag = null
                        isSuccessReturn = false
                    }
                }

                _state.updateIfProcessingStateEquals(
                    target = emitState,
                    to = duplicateTag?.let { TagEditState.Merge(from = emitState.tag, to = it) }
                )

                if (isSuccessReturn) Result.Success(Unit)
                else Result.Failure(IllegalStateException())
            }
        )
    }

    suspend fun mergeTag(): Result<Unit> {
        return _state.processingScope<TagEditState.Merge> { emitState ->
            tagRepository
                .save(SaveTagQuery.Modify(id = emitState.from.id, name = emitState.to.name))
                .map {}
        }
    }

    fun startDelete() {
        _state.updateIfTypeOf<TagEditState.Intro> { current -> TagEditState.Delete(current.tag, current.usageCount) }
    }

    suspend fun deleteTag(): Result<Unit> {
        return _state.processingScope<TagEditState.Delete> { emitState -> tagRepository.delete(emitState.tag.id) }
    }

    fun clearState() {
        _state.update { null }
    }
}