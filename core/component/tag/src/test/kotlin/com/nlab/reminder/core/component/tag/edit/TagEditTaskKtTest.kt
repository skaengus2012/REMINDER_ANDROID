package com.nlab.reminder.core.component.tag.edit

import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class TagEditTaskKtTest {
    @Test
    fun `Given any state, When create task, Then return not changed task`() = runTest {
        val state = genTagEditState()
        val actualTask = TagEditTask(state)
        assertThat(actualTask.current, sameInstance(state))
        assertThat(actualTask.next, equalTo(state))
        assertThat(actualTask.processAndGet(), equalTo(Result.success(state)))
    }

    @Test
    fun `Given await task selection, When create task with rename, Then return not changed task`() = runTest {
        val expectedState = genAwaitTaskSelectionState()
        val actualTask = TagEditTask<TagEditState.Rename>(expectedState) { error("That should not be called") }

        assertThat(actualTask.current, sameInstance(expectedState))
        assertThat(actualTask.next, equalTo(expectedState))
        assertThat(actualTask.processAndGet(), equalTo(Result.success(expectedState)))
    }

    @Test
    fun `Given rename task selection, When create task with rename, Then return matching state`() = runTest {
        val inputState = genRenameState()
        val expectedAfterProcessState = TagEditState.None
        val actualTask = TagEditTask<TagEditState.Rename>(inputState) {
            Result.success(expectedAfterProcessState)
        }
        assertThat(actualTask.next, equalTo(TagEditState.Processing(inputState)))
        assertThat(actualTask.processAndGet(), equalTo(Result.success(expectedAfterProcessState)))
    }
}
