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

import com.nlab.reminder.core.component.usermessage.errorMessage
import com.nlab.reminder.core.kotlin.Result
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.same

/**
 * @author Thalys
 */
class TagEditTaskExtKtTest {
    @Test
    fun `Given task success, When executeTagEditTask, Then updateState is called with correct states`() = runTest {
        val current = TagEditState.None
        val expectedStates = listOf(
            genAwaitTaskSelectionState(),
            genRenameState()
        )
        val task = TagEditTask(
            nextState = expectedStates[0],
            processAndGet = {
                Result.Success(expectedStates[1])
            }
        )

        val actualUpdatedStates = mutableListOf<TagEditState>()
        executeTagEditTask(
            task,
            current,
            updateState = { _, newState ->
                actualUpdatedStates += newState
            }
        )
        assertThat(
            actualUpdatedStates,
            equalTo(expectedStates)
        )
    }

    @Test
    fun `Given task fails, When executeTagEditTask, Then errorMessage is called with throwable`() = runTest {
        val throwable = Throwable()
        val task = TagEditTask(
            nextState = genTagEditState(),
            processAndGet = { Result.Failure(throwable) }
        )
        mockkStatic(::errorMessage)
        every { errorMessage(isNull(), isNull(), any()) } just Runs
        executeTagEditTask(
            task,
            genTagEditState(),
            updateState = { _, _ -> }
        )

        verify(exactly = 1) {
            errorMessage(
                isNull(),
                isNull(),
                same(throwable)
            )
        }

        unmockkStatic(::errorMessage)
    }
}