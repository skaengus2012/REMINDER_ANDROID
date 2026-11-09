package com.nlab.reminder.core.component.tag.edit

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.unconfinedTestDispatcher
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class GetTagEditTaskTransitionFlowUseCaseTest {
    private val getTagEditTaskTransitionFlow = GetTagEditTaskTransitionFlowUseCase()

    @Test
    fun `Given not changed task, When invoke, Then emit never`() = runTest {
        // given
        val state = genTagEditState()
        val task = TagEditTask(state)
        val expectedTransitions = emptyList<TagEditStateTransition>()

        // when
        val actualTransitions = mutableListOf<Result<TagEditStateTransition>>()
        backgroundScope.launch(unconfinedTestDispatcher()) {
            getTagEditTaskTransitionFlow(task).toList(actualTransitions)
        }

        // then
        assertThat(actualTransitions, equalTo(expectedTransitions))
    }

    @Test
    fun `Given task with next, When invoke, Then emit one with next`() = runTest {
        // given
        val current = genAwaitTaskSelectionState()
        val next = TagEditState.Processing(current)
        val task = TagEditTask(
            current = current,
            next = next,
            processAndGet = { Result.success(next) }
        )
        val expectedTransitions = listOf(
            Result.success(
                TagEditStateTransition(
                    previous = current,
                    updated = next
                )
            )
        )

        // when
        val actualTransitions = mutableListOf<Result<TagEditStateTransition>>()
        backgroundScope.launch(unconfinedTestDispatcher()) {
            getTagEditTaskTransitionFlow(task).toList(actualTransitions)
        }

        // then
        assertThat(actualTransitions, equalTo(expectedTransitions))
    }

    @Test
    fun `Given task with next and updated, When invoke, Then emit twice with next and updated`() = runTest {
        // given
        val current = genAwaitTaskSelectionState()
        val next = TagEditState.Processing(current)
        val updated = TagEditState.None
        val task = TagEditTask(
            current = current,
            next = next,
            processAndGet = { Result.success(updated) }
        )
        val expectedTransitions = listOf(
            Result.success(
                TagEditStateTransition(
                    previous = current,
                    updated = next
                )
            ),
            Result.success(
                TagEditStateTransition(
                    previous = next,
                    updated = updated
                )
            )
        )

        // when
        val actualTransitions = mutableListOf<Result<TagEditStateTransition>>()
        backgroundScope.launch(unconfinedTestDispatcher()) {
            getTagEditTaskTransitionFlow(task).toList(actualTransitions)
        }

        // then
        assertThat(actualTransitions, equalTo(expectedTransitions))
    }

    @Test
    fun `Given task with next and failure, When invoke, Then emit next, failure`() = runTest {
        // given
        val current = genAwaitTaskSelectionState()
        val next = TagEditState.Processing(current)
        val throwable = RuntimeException()
        val task = TagEditTask(
            current = current,
            next = next,
            processAndGet = { Result.failure(throwable) }
        )
        val expectedTransitions = listOf(
            Result.success(
                TagEditStateTransition(
                    previous = current,
                    updated = next
                )
            ),
            Result.failure(throwable)
        )

        // when
        val actualTransitions = mutableListOf<Result<TagEditStateTransition>>()
        backgroundScope.launch(unconfinedTestDispatcher()) {
            getTagEditTaskTransitionFlow(task).toList(actualTransitions)
        }

        // then
        assertThat(actualTransitions, equalTo(expectedTransitions))
    }
}