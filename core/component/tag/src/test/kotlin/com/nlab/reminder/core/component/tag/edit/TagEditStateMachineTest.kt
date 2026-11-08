package com.nlab.reminder.core.component.tag.edit

import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.TagId
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.data.model.genTags
import com.nlab.reminder.core.data.repository.SaveTagQuery
import com.nlab.reminder.core.data.repository.TagRepository
import com.nlab.reminder.core.kotlin.NonNegativeInt
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

    /**
     * Success: processAndGet is Rename with the usage count.
     * Failure: processAndGet is the failure from getUsageCount.
     */
    @Test
    fun `Given task selection, When start rename, Then Processing, Rename or failure`() = runTest {
        suspend fun verify(usageCountResult: Result<NonNegativeInt>) {
            val current = genAwaitTaskSelectionState()
            genTagEditStateMachine(
                tagRepository = mockk {
                    coEvery { getUsageCount(current.tag.id) } returns usageCountResult
                }
            ).startRename(current).run {
                assertThat(next, equalTo(TagEditState.Processing(current)))
                assertThat(
                    processAndGet(),
                    equalTo(
                        usageCountResult.map {
                            TagEditState.Rename(
                                tag = current.tag,
                                usageCount = it,
                                renameText = current.tag.name.value,
                                shouldUserInputReady = true
                            )
                        }
                    )
                )
            }
        }

        verify(Result.success(genNonNegativeInt()))
        verify(Result.failure(IllegalStateException()))
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
        assertThat(actualTask.next, equalTo(current))
        assertThat(actualTask.processAndGet(), equalTo(Result.success(current)))
    }

    @Test
    fun `Given rename with blank text, When try update name, Then return task that changed to None`() = runTest {
        val current = genRenameState(renameText = genBlank())
        val tagEditStateMachine = genTagEditStateMachine()
        val actualTask = tagEditStateMachine.tryUpdateName(current, genTags())
        assertThat(actualTask.next, equalTo(TagEditState.None))
        assertThat(actualTask.processAndGet(), equalTo(Result.success(TagEditState.None)))
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
        assertThat(actualTask.next, equalTo(TagEditState.None))
        assertThat(actualTask.processAndGet(), equalTo(Result.success(TagEditState.None)))
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
        assertThat(actualTask.next, equalTo(expectedState))
        assertThat(actualTask.processAndGet(), equalTo(Result.success(expectedState)))
    }

    /**
     * Success: processAndGet is None.
     * Failure: processAndGet is the failure from save.
     */
    @Test
    fun `Given valid rename, When try update name, Then Processing, None or failure`() = runTest {
        suspend fun verify(saveResult: Result<Tag>) {
            val current = genRenameState()
            val query = SaveTagQuery.Modify(
                id = current.tag.id,
                name = current.renameText.toNonBlankString(),
                shouldMergeIfExists = false
            )
            genTagEditStateMachine(
                tagRepository = mockk { coEvery { save(query) } returns saveResult }
            ).tryUpdateName(current, compareTags = emptySet()).run {
                assertThat(next, equalTo(TagEditState.Processing(current)))
                assertThat(processAndGet(), equalTo(saveResult.map { TagEditState.None }))
            }
        }

        verify(Result.success(genTag()))
        verify(Result.failure(IllegalStateException()))
    }

    /**
     * Success: processAndGet is None.
     * Failure: processAndGet is the failure from save.
     */
    @Test
    fun `Given merge state, When try merge tag, Then Processing, None or failure`() = runTest {
        suspend fun verify(saveResult: Result<Tag>) {
            val current = genMergeState(from = genTag(), to = genTag())
            val query = SaveTagQuery.Modify(
                id = current.from.id,
                name = current.to.name,
                shouldMergeIfExists = true
            )
            genTagEditStateMachine(
                tagRepository = mockk { coEvery { save(query) } returns saveResult }
            ).merge(current).run {
                assertThat(next, equalTo(TagEditState.Processing(current)))
                assertThat(processAndGet(), equalTo(saveResult.map { TagEditState.None }))
            }
        }

        verify(Result.success(genTag()))
        verify(Result.failure(IllegalStateException()))
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

    /**
     * Success: processAndGet is Delete with the usage count.
     * Failure: processAndGet is the failure from getUsageCount.
     */
    @Test
    fun `Given task selection, When start delete, Then Processing, Delete or failure`() = runTest {
        suspend fun verify(usageCountResult: Result<NonNegativeInt>) {
            val current = genAwaitTaskSelectionState()
            genTagEditStateMachine(
                tagRepository = mockk {
                    coEvery { getUsageCount(current.tag.id) } returns usageCountResult
                }
            ).startDelete(current).run {
                assertThat(next, equalTo(TagEditState.Processing(current)))
                assertThat(
                    processAndGet(),
                    equalTo(
                        usageCountResult.map {
                            TagEditState.Delete(tag = current.tag, usageCount = it)
                        }
                    )
                )
            }
        }

        verify(Result.success(genNonNegativeInt()))
        verify(Result.failure(IllegalStateException()))
    }

    /**
     * Success: processAndGet is None.
     * Failure: processAndGet is the failure from delete.
     */
    @Test
    fun `Given delete state, When try delete, Then Processing, None or failure`() = runTest {
        suspend fun verify(deleteResult: Result<Unit>) {
            val current = genDeleteState()
            genTagEditStateMachine(
                tagRepository = mockk { coEvery { delete(current.tag.id) } returns deleteResult }
            ).delete(current).run {
                assertThat(next, equalTo(TagEditState.Processing(current)))
                assertThat(processAndGet(), equalTo(deleteResult.map { TagEditState.None }))
            }
        }

        verify(Result.success(Unit))
        verify(Result.failure(IllegalStateException()))
    }
}

private fun genTagEditStateMachine(
    tagRepository: TagRepository = mockk()
): TagEditStateMachine = TagEditStateMachine(tagRepository = tagRepository)