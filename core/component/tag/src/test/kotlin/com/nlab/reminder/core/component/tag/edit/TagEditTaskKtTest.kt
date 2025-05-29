package com.nlab.reminder.core.component.tag.edit

import com.nlab.reminder.core.kotlin.Result
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.unconfinedTestDispatcher
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
        assertThat(actualTask.processAndGet(), equalTo(Result.Success(state)))
    }

    @Test
    fun `Given await task selection, When create task with rename, Then return not changed task`() = runTest {
        val expectedState = genAwaitTaskSelectionState()
        val actualTask = TagEditTask<TagEditState.Rename>(expectedState) { error("That should not be called") }

        assertThat(actualTask.current, sameInstance(expectedState))
        assertThat(actualTask.next, equalTo(expectedState))
        assertThat(actualTask.processAndGet(), equalTo(Result.Success(expectedState)))
    }

    @Test
    fun `Given rename task selection, When create task with rename, Then return matching state`() = runTest {
        val inputState = genRenameState()
        val expectedAfterProcessState = TagEditState.None
        val actualTask = TagEditTask<TagEditState.Rename>(inputState) {
            Result.Success(expectedAfterProcessState)
        }
        assertThat(actualTask.next, equalTo(TagEditState.Processing(inputState)))
        assertThat(actualTask.processAndGet(), equalTo(Result.Success(expectedAfterProcessState)))
    }

    @Test
    fun `Given not changed task, When process as flow, Then emit never`() = runTest {
        // given
        val state = genTagEditState()
        val task = TagEditTask(state)
        val expectedTransitions = emptyList<TagEditStateTransition>()

        // when
        val actualTransitions = mutableListOf<Result<TagEditStateTransition>>()
        backgroundScope.launch(unconfinedTestDispatcher()) {
            task.processAsFlow().toList(actualTransitions)
        }

        // then
        assertThat(actualTransitions, equalTo(expectedTransitions))
    }

    @Test
    fun `Given task with next, When process as flow, Then emit one with next`() = runTest {
        // given
        val current = genAwaitTaskSelectionState()
        val next = TagEditState.Processing(current)
        val task = TagEditTask(
            current = current,
            next = next,
            processAndGet = { Result.Success(next) }
        )
        val expectedTransitions = listOf(
            Result.Success(
                TagEditStateTransition(
                    previous = current,
                    updated = next
                )
            )
        )

        // when
        val actualTransitions = mutableListOf<Result<TagEditStateTransition>>()
        backgroundScope.launch(unconfinedTestDispatcher()) {
            task.processAsFlow().toList(actualTransitions)
        }

        // then
        assertThat(actualTransitions, equalTo(expectedTransitions))
    }

    @Test
    fun `Given task with next and updated, When process as flow, Then emit twice with next and updated`() = runTest {
        // given
        val current = genAwaitTaskSelectionState()
        val next = TagEditState.Processing(current)
        val updated = TagEditState.None
        val task = TagEditTask(
            current = current,
            next = next,
            processAndGet = { Result.Success(updated) }
        )
        val expectedTransitions = listOf(
            Result.Success(
                TagEditStateTransition(
                    previous = current,
                    updated = next
                )
            ),
            Result.Success(
                TagEditStateTransition(
                    previous = next,
                    updated = updated
                )
            )
        )

        // when
        val actualTransitions = mutableListOf<Result<TagEditStateTransition>>()
        backgroundScope.launch(unconfinedTestDispatcher()) {
            task.processAsFlow().toList(actualTransitions)
        }

        // then
        assertThat(actualTransitions, equalTo(expectedTransitions))
    }

    @Test
    fun `Given task with next and failure, When process as flow, Then emit next, failure`() = runTest {
        // given
        val current = genAwaitTaskSelectionState()
        val next = TagEditState.Processing(current)
        val throwable = RuntimeException()
        val task = TagEditTask(
            current = current,
            next = next,
            processAndGet = { Result.Failure(throwable) }
        )
        val expectedTransitions = listOf(
            Result.Success(
                TagEditStateTransition(
                    previous = current,
                    updated = next
                )
            ),
            Result.Failure(throwable)
        )

        // when
        val actualTransitions = mutableListOf<Result<TagEditStateTransition>>()
        backgroundScope.launch(unconfinedTestDispatcher()) {
            task.processAsFlow().toList(actualTransitions)
        }

        // then
        assertThat(actualTransitions, equalTo(expectedTransitions))
    }
}