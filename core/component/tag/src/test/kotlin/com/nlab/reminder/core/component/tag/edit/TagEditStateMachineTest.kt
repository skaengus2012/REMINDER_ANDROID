package com.nlab.reminder.core.component.tag.edit

import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.data.model.genTagId
import com.nlab.reminder.core.data.model.genTags
import com.nlab.reminder.core.data.repository.SaveTagQuery
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.faker.genNonBlankString
import com.nlab.reminder.core.kotlin.faker.genNonNegativeInt
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.testkit.faker.genBlank
import com.nlab.testkit.faker.genBothify
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class TagEditStateMachineTest {
    @Test
    fun `Given none and tag, When start editing, Then return AwaitTaskSelection`() {
        val current = TagEditState.None
        val tag = genTag()
        val tagEditStateMachine = genTagEditStateMachine()
        val actualState = tagEditStateMachine.startEditing(current, tag)
        assertThat(actualState, equalTo(TagEditState.AwaitTaskSelection(tag)))
    }

    @Test
    fun `Given not none, When start editing, Then return same state`() {
        val current = genTagEditStateExcludeTypeOf<TagEditState.None>()
        val tagEditStateMachine = genTagEditStateMachine()
        val actualState = tagEditStateMachine.startEditing(current, genTag())
        assertThat(actualState, equalTo(current))
    }

    @Test
    fun `Given await task selection, When start editing, Then return task that changed to Processing, Rename`() = runTest {
        val current = genAwaitTaskSelectionState()
        val usageCount = genNonNegativeInt()
        val expectedAfterProcessState = TagEditState.Rename(
            tag = current.tag,
            usageCount = usageCount,
            renameText = current.tag.name.value,
            shouldUserInputReady = true
        )
        val tagEditStateMachine = genTagEditStateMachine(
            tagRepository = mockk {
                coEvery { getUsageCount(current.tag.id) } returns Result.Success(usageCount)
            }
        )
        val actualTask = tagEditStateMachine.startRename(current)
        assertThat(actualTask.nextState, equalTo(TagEditState.Processing(current)))
        assertThat(actualTask.processAndGet(), equalTo(Result.Success(expectedAfterProcessState)))
    }

    @Test
    fun `Given rename, When ready rename input, Then return state that changed shouldUserInputReady to false`() = runTest {
        val current = genRenameState(shouldUserInputReady = true)
        val tagEditStateMachine = genTagEditStateMachine()
        val actualState = tagEditStateMachine.readyRenameInput(current)
        assertThat(actualState, equalTo(current.copy(shouldUserInputReady = false)))
    }

    @Test
    fun `Given not rename, When ready rename input, Then return same state`() = runTest {
        val current = genTagEditStateExcludeTypeOf<TagEditState.Rename>()
        val tagEditStateMachine = genTagEditStateMachine()
        val actualState = tagEditStateMachine.readyRenameInput(current)
        assertThat(actualState, equalTo(current))
    }

    @Test
    fun `Given rename, When change rename input, Then return state that changed renameText`() {
        val current = genRenameState(renameText = "")
        val input = genBothify()
        val tagEditStateMachine = genTagEditStateMachine()
        val actualState = tagEditStateMachine.changeRenameText(current, input)
        assertThat(actualState, equalTo(current.copy(renameText = input)))
    }

    @Test
    fun `Given not rename, When change rename input, Then return same state`() {
        val current = genTagEditStateExcludeTypeOf<TagEditState.Rename>()
        val input = genBothify()
        val tagEditStateMachine = genTagEditStateMachine()
        val actualState = tagEditStateMachine.changeRenameText(current, input)
        assertThat(actualState, equalTo(current))
    }

    @Test
    fun `Given not rename, When try update name, Then return not changed task`() = runTest {
        val current = genTagEditStateExcludeTypeOf<TagEditState.Rename>()
        val tagEditStateMachine = genTagEditStateMachine()
        val actualTask = tagEditStateMachine.tryUpdateName(current, genTags())
        assertThat(actualTask.nextState, equalTo(current))
        assertThat(actualTask.processAndGet(), equalTo(Result.Success(current)))
    }

    @Test
    fun `Given rename with blank text, When try update name, Then return task that changed to None`() = runTest {
        val current = genRenameState(renameText = genBlank())
        val tagEditStateMachine = genTagEditStateMachine()
        val actualTask = tagEditStateMachine.tryUpdateName(current, genTags())
        assertThat(actualTask.nextState, equalTo(TagEditState.None))
        assertThat(actualTask.processAndGet(), equalTo(Result.Success(TagEditState.None)))
    }

    @Test
    fun `Given rename with not changed name, When try update name, Then return task that changed to None`() = runTest {
        val tag = genTag()
        val current = genRenameState(
            tag = tag,
            renameText = tag.name.value
        )
        val compareTags = setOf(tag)
        val tagEditStateMachine = genTagEditStateMachine()
        val actualTask = tagEditStateMachine.tryUpdateName(current, compareTags)
        assertThat(actualTask.nextState, equalTo(TagEditState.None))
        assertThat(actualTask.processAndGet(), equalTo(Result.Success(TagEditState.None)))
    }

    @Test
    fun `Given rename with duplicated name, When try update name, Then return task that changed to Merge`() = runTest {
        val targetTag = genTag()
        val duplicateTag = Tag(
            id = TagId(targetTag.id.rawId + 1),
            name = "duplicated ${targetTag.name}".toNonBlankString()
        )
        val current = genRenameState(
            tag = targetTag,
            renameText = duplicateTag.name.value
        )
        val compareTags = setOf(targetTag, duplicateTag)
        val expectedState = TagEditState.Merge(
            from = targetTag,
            fromUsageCount = current.usageCount,
            to = duplicateTag
        )

        val tagEditStateMachine = genTagEditStateMachine()
        val actualTask = tagEditStateMachine.tryUpdateName(current, compareTags)
        assertThat(actualTask.nextState, equalTo(expectedState))
        assertThat(actualTask.processAndGet(), equalTo(Result.Success(expectedState)))
    }

    @Test
    fun `Given valid rename, When try update name, Then return task that changes to Processing, None`() = runTest {
        val current = genRenameState()
        val expectedAfterProcessState = TagEditState.None
        val tagEditStateMachine = genTagEditStateMachine(
            tagRepository = mockk {
                val query = SaveTagQuery.Modify(
                    id = current.tag.id,
                    name = current.renameText.toNonBlankString(),
                    shouldMergeIfExists = false
                )
                coEvery { save(query) } returns Result.Success(
                    current.tag.copy(name = current.renameText.toNonBlankString())
                )
            }
        )
        val actualTask = tagEditStateMachine.tryUpdateName(current, compareTags = emptySet())
        assertThat(
            actualTask.nextState,
            equalTo(TagEditState.Processing(current))
        )
        assertThat(
            actualTask.processAndGet(),
            equalTo(Result.Success(expectedAfterProcessState))
        )
    }

    @Test
    fun `Given merge state, When try merge tag, Then return task that changes to Processing, None`() = runTest {
        val targetTagId = genTagId()
        val targetName = genNonBlankString()
        val current = genMergeState(from = genTag(id = targetTagId), to = genTag(name = targetName))
        val expectedAfterProcessState = TagEditState.None
        val tagEditStateMachine = genTagEditStateMachine(
            tagRepository = mockk {
                val query = SaveTagQuery.Modify(
                    id = targetTagId,
                    name = targetName,
                    shouldMergeIfExists = true
                )
                coEvery { save(query) } returns Result.Success(current.to)
            }
        )
        val actualTask = tagEditStateMachine.merge(current)
        assertThat(
            actualTask.nextState,
            equalTo(TagEditState.Processing(current))
        )
        assertThat(
            actualTask.processAndGet(),
            equalTo(Result.Success(expectedAfterProcessState))
        )
    }

    @Test
    fun `Given merge state, When cancel merge, Then return rename`() {
        val current = genMergeState()
        val expectedState = TagEditState.Rename(
            tag = current.from,
            usageCount = current.fromUsageCount,
            renameText = current.to.name.value,
            shouldUserInputReady = true
        )
        val actualState = genTagEditStateMachine().cancelMerge(current)
        assertThat(actualState, equalTo(expectedState))
    }

    @Test
    fun `Given not merge state, When cancel merge, Then return same state`() {
        val current = genTagEditStateExcludeTypeOf<TagEditState.Merge>()
        val actualState =  genTagEditStateMachine().cancelMerge(current)
        assertThat(actualState, equalTo(current))
    }

    @Test
    fun `Given await task selection, When start delete tag, Then return task that changes to Processing, Delete`() = runTest {
        val current = genAwaitTaskSelectionState()
        val usageCount = genNonNegativeInt()
        val expectedAfterProcessState = TagEditState.Delete(
            tag = current.tag,
            usageCount = usageCount
        )
        val tagEditStateMachine = genTagEditStateMachine(
            tagRepository = mockk {
                coEvery { getUsageCount(current.tag.id) } returns Result.Success(usageCount)
            }
        )
        val actualTask = tagEditStateMachine.startDelete(current)
        assertThat(actualTask.nextState, equalTo(TagEditState.Processing(current)))
        assertThat(actualTask.processAndGet(), equalTo(Result.Success(expectedAfterProcessState)))
    }

    @Test
    fun `Given delete state, When try delete, Then return task that changes to Processing, None`() = runTest {
        val current = genDeleteState()
        val tagEditStateMachine = genTagEditStateMachine(
            tagRepository = mockk {
                coEvery { delete(current.tag.id) } returns Result.Success(Unit)
            }
        )
        val actualTask = tagEditStateMachine.delete(current)
        assertThat(actualTask.nextState, equalTo(TagEditState.Processing(current)))
        assertThat(actualTask.processAndGet(), equalTo(Result.Success(TagEditState.None)))
    }
}

private fun genTagEditStateMachine(
    tagRepository: TagRepository = mockk()
): TagEditStateMachine = TagEditStateMachine(tagRepository = tagRepository)