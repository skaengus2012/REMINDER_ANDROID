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

package com.nlab.reminder.feature.home

import com.nlab.reminder.core.component.tag.edit.TagEditState
import com.nlab.reminder.core.component.tag.edit.TagEditStateMachine
import com.nlab.reminder.core.component.tag.edit.TagEditTask
import com.nlab.reminder.core.component.tag.edit.genTagEditState
import com.nlab.reminder.core.component.tag.edit.genTagEditStateExcludeTypeOf
import com.nlab.reminder.core.component.tag.edit.genTagEditStateTypeOf
import com.nlab.reminder.core.component.usermessage.errorMessage
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.kotlin.Result
import com.nlab.statekit.test.reduce.effectScenario
import com.nlab.statekit.test.reduce.expectedStateToInit
import com.nlab.statekit.test.reduce.launchAndJoin
import com.nlab.statekit.test.reduce.transitionScenario
import com.nlab.testkit.faker.genBothify
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class HomeReduceKtTest {
    @Test
    fun `Given loading, When stateSynced received, Then become Success`() = runTest {
        genHomeReduce()
            .transitionScenario()
            .initState(HomeUiState.Loading)
            .action(genHomeActionStateSynced())
            .expectedStateFromInput {
                HomeUiState.Success(
                    todayScheduleCount = action.todaySchedulesCount,
                    timetableScheduleCount = action.timetableSchedulesCount,
                    allScheduleCount = action.allSchedulesCount,
                    tags = action.sortedTags,
                    tagEditState = TagEditState.None
                )
            }
            .verify()
    }

    @Test
    fun `Given success, When stateSynced received, Then update fields from action`() = runTest {
        genHomeReduce()
            .transitionScenario()
            .initState(genHomeUiStateSuccess())
            .action(genHomeActionStateSynced())
            .expectedStateFromInput {
                HomeUiState.Success(
                    todayScheduleCount = action.todaySchedulesCount,
                    timetableScheduleCount = action.timetableSchedulesCount,
                    allScheduleCount = action.allSchedulesCount,
                    tags = action.sortedTags,
                    tagEditState = initState.tagEditState,
                )
            }
            .verify()
    }

    @Test
    fun `Given success, tagEditState matches expected, When CAS invoked, Then update state`() = runTest {
        val currentTagEditState = genTagEditStateTypeOf<TagEditState.None>()
        val nextTagEditState = genTagEditStateExcludeTypeOf<TagEditState.AwaitTaskSelection>()
        genHomeReduce()
            .transitionScenario()
            .initState(genHomeUiStateSuccess(tagEditState = currentTagEditState))
            .action(
                HomeAction.CompareAndSetTagEditState(
                    expectedState = currentTagEditState,
                    newState = nextTagEditState
                )
            )
            .expectedStateFromInput { initState.copy(tagEditState = nextTagEditState) }
            .verify()
    }

    @Test
    fun `Given success, tagEditState does not match expected, When CAS invoked, Then don't update state`() = runTest {
        val currentTagEditState = genTagEditStateTypeOf<TagEditState.Rename>()
        genHomeReduce()
            .transitionScenario()
            .initState(genHomeUiStateSuccess(tagEditState = currentTagEditState))
            .action(
                HomeAction.CompareAndSetTagEditState(
                    expectedState = TagEditState.None,
                    newState = TagEditState.None
                )
            )
            .expectedStateToInit()
            .verify()
    }

    @Test
    fun `Given success, When tag long clicked, Then update by stateMachine result`() = runTest {
        val tag = genTag()
        assertTagEditStateTransformedByStateMachine(
            action = HomeAction.OnTagLongClicked(tag),
            buildStateMachine = {
                mockk { every { startEditing(current.tagEditState, tag) } returns nextTagEditState }
            }
        )
    }

    @Test
    fun `Given success, When rename requested, Then dispatch CAS actions`() = runTest {
        assertTagEditTaskDispatchedWithStateMachine(
            action = HomeAction.OnTagRenameRequestClicked,
            buildStateMachine = {
                mockk { every { startRename(current.tagEditState) } returns tagEditTaskStub }
            }
        )
    }

    @Test
    fun `Given success, rename loading fails, When rename requested, Then throw exception`() = runTest {
        assertTagEditTaskThrowsErrorWithStateMachine(
            action = HomeAction.OnTagRenameRequestClicked,
            buildStateMachine = {
                mockk { every { startRename(any()) } returns tagEditTaskStub }
            }
        )
    }

    @Test
    fun `Given success, When rename input ready, Then update by stateMachine result`() = runTest {
        assertTagEditStateTransformedByStateMachine(
            action = HomeAction.OnTagRenameInputReady,
            buildStateMachine = {
                mockk { every { readyRenameInput(current.tagEditState) } returns nextTagEditState }
            }
        )
    }

    @Test
    fun `Given success, When rename inputted, Then update by stateMachine result`() = runTest {
        val input = genBothify()
        assertTagEditStateTransformedByStateMachine(
            action = HomeAction.OnTagRenameInputted(input),
            buildStateMachine = {
                mockk { every { changeRenameText(current.tagEditState, input) } returns nextTagEditState }
            }
        )
    }

    @Test
    fun `Given success, When rename confirmed, Then dispatch CAS actions`() = runTest {
        assertTagEditTaskDispatchedWithStateMachine(
            action = HomeAction.OnTagRenameConfirmClicked,
            buildStateMachine = {
                mockk { every { tryUpdateName(current.tagEditState, current.tags) } returns tagEditTaskStub }
            }
        )
    }



    @Test
    fun `Given success, When replace confirmed, Then dispatch CAS actions`() = runTest {
        assertTagEditTaskDispatchedWithStateMachine(
            action = HomeAction.OnTagReplaceConfirmClicked,
            buildStateMachine = {
                mockk { every { merge(current.tagEditState) } returns tagEditTaskStub }
            }
        )
    }



    @Test
    fun `Given success, When replace canceled, Then update by stateMachine result`() = runTest {
        assertTagEditStateTransformedByStateMachine(
            action = HomeAction.OnTagReplaceCancelClicked,
            buildStateMachine = {
                mockk { every { cancelMerge(current.tagEditState) } returns nextTagEditState }
            }
        )
    }

    @Test
    fun `Given success state, When delete requested, Then dispatch CAS actions`() = runTest {
        assertTagEditTaskDispatchedWithStateMachine(
            action = HomeAction.OnTagDeleteRequestClicked,
            buildStateMachine = {
                mockk { every { startDelete(current.tagEditState) } returns tagEditTaskStub }
            }
        )
    }



    @Test
    fun `Given success, When delete confirmed, Then dispatch CAS actions`() = runTest {
        assertTagEditTaskDispatchedWithStateMachine(
            action = HomeAction.OnTagDeleteConfirmClicked,
            buildStateMachine = {
                mockk { every { delete(current.tagEditState) } returns tagEditTaskStub }
            }
        )
    }

}

private fun genHomeReduce(environment: HomeEnvironment = genHomeEnvironment()): HomeReduce = HomeReduce(environment)

private data class TagEditTransformAssertParam(
    val current: HomeUiState.Success,
    val nextTagEditState: TagEditState
)

private suspend fun assertTagEditStateTransformedByStateMachine(
    action: HomeAction,
    buildStateMachine: TagEditTransformAssertParam.() -> TagEditStateMachine
) {
    val currentTagEditState = genTagEditStateTypeOf<TagEditState.None>()
    val nextTagEditState = genTagEditStateExcludeTypeOf<TagEditState.AwaitTaskSelection>()
    val initState = genHomeUiStateSuccess(tagEditState = currentTagEditState)
    val tagEditStateMachine = buildStateMachine(
        TagEditTransformAssertParam(
            initState,
            nextTagEditState
        )
    )
    genHomeReduce(environment = genHomeEnvironment(tagEditStateMachine = tagEditStateMachine))
        .transitionScenario()
        .initState(initState)
        .action(action)
        .expectedStateFromInput { initState.copy(tagEditState = nextTagEditState) }
        .verify()
}

private data class TagEditTaskExecutionAssertParam(
    val current: HomeUiState.Success,
    val tagEditTaskStub: TagEditTask
)

private suspend fun assertTagEditTaskDispatchedWithStateMachine(
    action: HomeAction,
    buildStateMachine: TagEditTaskExecutionAssertParam.() -> TagEditStateMachine
) {
    val currentTagEditState = genTagEditStateTypeOf<TagEditState.None>()
    val nextTagEditState = genTagEditStateExcludeTypeOf<TagEditState.None>()
    val afterProcessTagEditTask = genTagEditStateExcludeTypeOf(TagEditState.None::class, nextTagEditState::class)
    val initState = genHomeUiStateSuccess(tagEditState = currentTagEditState)
    val tagEditStateMachine = buildStateMachine(
        TagEditTaskExecutionAssertParam(
            initState,
            TagEditTask(
                nextState = nextTagEditState,
                processAndGet = { Result.Success(afterProcessTagEditTask) }
            )
        )
    )
    val actualDispatchedActions = mutableListOf<HomeAction>()
    genHomeReduce(environment = genHomeEnvironment(tagEditStateMachine = tagEditStateMachine))
        .effectScenario()
        .initState(initState)
        .action(action)
        .hook { actualDispatchedActions += this.action }
        .launchAndJoin()
    assertThat(
        actualDispatchedActions, equalTo(
            listOf(
                action,
                HomeAction.CompareAndSetTagEditState(currentTagEditState, nextTagEditState),
                HomeAction.CompareAndSetTagEditState(nextTagEditState, afterProcessTagEditTask)
            )
        )
    )
}

private suspend fun assertTagEditTaskThrowsErrorWithStateMachine(
    action: HomeAction,
    buildStateMachine: TagEditTaskExecutionAssertParam.() -> TagEditStateMachine
) {
   mockkStatic(::errorMessage)
    every { errorMessage(any()) } just Runs

    val initState = genHomeUiStateSuccess()
    val tagEditStateMachine = buildStateMachine(
        TagEditTaskExecutionAssertParam(
            initState,
            TagEditTask(
                nextState = genTagEditState(),
                processAndGet = { Result.Failure(IllegalStateException()) }
            )
        )
    )
    genHomeReduce(environment = genHomeEnvironment(tagEditStateMachine = tagEditStateMachine))
        .effectScenario()
        .initState(initState)
        .action(action)
        .launchAndJoin()
    verify(exactly = 1) { errorMessage(any()) }
}