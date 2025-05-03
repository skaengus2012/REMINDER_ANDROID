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
import com.nlab.reminder.core.kotlin.map
import com.nlab.reminder.core.kotlin.tryToNonBlankStringOrNull

/**
 * @author Doohyun
 */
class TagEditStateMachine(private val tagRepository: TagRepository) {
    fun startEditing(current: TagEditState, tag: Tag): TagEditState = transformIfTypeOf<TagEditState.None>(
        current,
        transform = { TagEditState.AwaitTaskSelection(tag) }
    )

    fun startRename(current: TagEditState): TagEditTask = TagEditTask<TagEditState.AwaitTaskSelection>(
        current,
        processAndGet = { awaitTaskSelection ->
            tagRepository.getUsageCount(tagId = awaitTaskSelection.tag.id).map { usageCount ->
                TagEditState.Rename(
                    tag = awaitTaskSelection.tag,
                    usageCount = usageCount,
                    renameText = awaitTaskSelection.tag.name.value,
                    shouldUserInputReady = true
                )
            }
        }
    )

    fun readyRenameInput(current: TagEditState): TagEditState = transformIfTypeOf<TagEditState.Rename>(
        current,
        transform = { it.copy(shouldUserInputReady = false) }
    )

    fun changeRenameText(current: TagEditState, input: String): TagEditState = transformIfTypeOf<TagEditState.Rename>(
        current,
        transform = { it.copy(renameText = input) }
    )

    fun tryUpdateName(current: TagEditState, compareTags: Collection<Tag>): TagEditTask {
        if (current !is TagEditState.Rename) return TagEditTask(current)

        val newName = current.renameText.tryToNonBlankStringOrNull() ?: return TagEditTask(TagEditState.None)
        val sameNameTags = compareTags.filter { it.name == newName }
        if (sameNameTags.isNotEmpty()) {
            return TagEditTask(
                if (sameNameTags.any { it.id == current.tag.id }) TagEditState.None
                else TagEditState.Merge(
                    from = current.tag,
                    fromUsageCount = current.usageCount,
                    to = sameNameTags.first()
                )
            )
        }

        return TagEditTask(
            nextState = TagEditState.Processing(current),
            processAndGet = {
                tagRepository
                    .save(SaveTagQuery.Modify(id = current.tag.id, name = newName, shouldMergeIfExists = false))
                    .map { TagEditState.None }
            }
        )
    }

    fun merge(current: TagEditState): TagEditTask = TagEditTask<TagEditState.Merge>(
        current,
        processAndGet = { merge ->
            tagRepository
                .save(SaveTagQuery.Modify(id = merge.from.id, name = merge.to.name, shouldMergeIfExists = true))
                .map { TagEditState.None }
        }
    )

    fun cancelMerge(current: TagEditState): TagEditState = transformIfTypeOf<TagEditState.Merge>(
        current,
        transform = { merge ->
            TagEditState.Rename(
                tag = merge.from,
                usageCount = merge.fromUsageCount,
                renameText = merge.to.name.value,
                shouldUserInputReady = true
            )
        }
    )

    fun startDelete(current: TagEditState): TagEditTask = TagEditTask<TagEditState.AwaitTaskSelection>(
        current,
        processAndGet = { awaitTaskSelection ->
            tagRepository.getUsageCount(tagId = awaitTaskSelection.tag.id).map { usageCount ->
                TagEditState.Delete(
                    tag = awaitTaskSelection.tag,
                    usageCount = usageCount
                )
            }
        }
    )

    fun delete(current: TagEditState): TagEditTask = TagEditTask<TagEditState.Delete>(
        current,
        processAndGet = { delete ->
            tagRepository.delete(delete.tag.id).map { TagEditState.None }
        }
    )
}

private inline fun <reified T : TagEditState> transformIfTypeOf(
    current: TagEditState,
    transform: (T) -> TagEditState
): TagEditState = if (current is T) transform(current) else current