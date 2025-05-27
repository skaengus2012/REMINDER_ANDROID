/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

import app.cash.turbine.test
import com.nlab.reminder.core.component.usermessage.UserMessageException
import com.nlab.reminder.core.component.usermessage.UserMessageFactory
import com.nlab.reminder.core.component.usermessage.genUserMessageExceptionSource
import com.nlab.reminder.core.kotlin.Result
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class TagEditTaskExecutorTest {
    @Test
    fun `Given initial, task with next states, When processed, Then emits state transitions in order`() = runTest {
        // given
        val initialState = TagEditState.None
        val intermediateState = genAwaitTaskSelectionState()
        val finalState = genRenameState()
        val task = TagEditTask(
            nextState = intermediateState,
            processAndGet = { Result.Success(finalState) }
        )
        val tagEditTaskExecutor = genTagEditTaskExecutor()

        // when
        tagEditTaskExecutor.process(task, initialState).test {
            // then
            assertThat(
                awaitItem(), equalTo(
                    TagEditStateTransition(previousState = initialState, updatedState = intermediateState)
                )
            )
            assertThat(
                awaitItem(), equalTo(
                    TagEditStateTransition(previousState = intermediateState, updatedState = finalState)
                )
            )
            awaitComplete()
        }
    }

    @Test
    fun `Given failing task, When processed, Then throws UserMessageException with expected content`() = runTest {
        // given
        val throwable = Throwable()
        val task = TagEditTask(
            nextState = genTagEditState(),
            processAndGet = { Result.Failure(throwable) }
        )
        val expectedExceptionSource = genUserMessageExceptionSource()
        val tagEditTaskExecutor = genTagEditTaskExecutor(
            userMessageFactory = mockk {
                every { createExceptionSource(message = null, priority = null) } returns expectedExceptionSource
            }
        )

        // when
        tagEditTaskExecutor.process(task, genTagEditState()).test {
            // then
            skipItems(count = 1) // skip intermediateState

            val actualException = awaitError() as UserMessageException
            assertThat(
                actualException.origin,
                sameInstance(throwable)
            )
            assertThat(
                actualException.userMessage.message,
                equalTo(expectedExceptionSource.message)
            )
            assertThat(
                actualException.userMessage.priority,
                equalTo(expectedExceptionSource.priority)
            )
        }
    }
}

private fun genTagEditTaskExecutor(
    userMessageFactory: UserMessageFactory = mockk()
): TagEditTaskExecutor = TagEditTaskExecutor(
    userMessageFactory = userMessageFactory
)