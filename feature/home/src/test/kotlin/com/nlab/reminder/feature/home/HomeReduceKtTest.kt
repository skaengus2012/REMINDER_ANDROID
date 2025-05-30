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
import com.nlab.reminder.core.component.tag.edit.TagEditStateTransition
import com.nlab.reminder.core.component.tag.edit.TagEditTask
import com.nlab.reminder.core.component.tag.edit.genTagEditStateExcludeTypeOf
import com.nlab.reminder.core.component.tag.edit.genTagEditStateTypeOf
import com.nlab.reminder.core.component.tag.edit.processAsFlow
import com.nlab.reminder.core.component.usermessage.UserMessageException
import com.nlab.reminder.core.component.usermessage.UserMessageFactory
import com.nlab.reminder.core.component.usermessage.genUserMessageExceptionSource
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.kotlin.Result
import com.nlab.statekit.test.reduce.effectScenario
import com.nlab.statekit.test.reduce.expectedStateToInit
import com.nlab.statekit.test.reduce.launchAndJoin
import com.nlab.statekit.test.reduce.transitionScenario
import com.nlab.testkit.faker.genBothify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
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
        assertTagEditStateTransform(
            action = HomeAction.OnTagLongClicked(tag),
            buildStateMachine = {
                mockk { every { startEditing(current.tagEditState, tag) } returns nextTagEditState }
            }
        )
    }

    @Test
    fun `Given success, When rename requested, Then dispatch CAS actions`() = runTest {
        assertTagEditTaskProcess(
            action = HomeAction.OnTagRenameRequestClicked,
            buildTagEditStateMachine = {
                mockk { every { startRename(current.tagEditState) } returns tagEditTaskStub }
            }
        )
    }

    @Test
    fun `Given success, When rename input ready, Then update by stateMachine result`() = runTest {
        assertTagEditStateTransform(
            action = HomeAction.OnTagRenameInputReady,
            buildStateMachine = {
                mockk { every { readyRenameInput(current.tagEditState) } returns nextTagEditState }
            }
        )
    }

    @Test
    fun `Given success, When rename inputted, Then update by stateMachine result`() = runTest {
        val input = genBothify()
        assertTagEditStateTransform(
            action = HomeAction.OnTagRenameInputted(input),
            buildStateMachine = {
                mockk { every { changeRenameText(current.tagEditState, input) } returns nextTagEditState }
            }
        )
    }

    @Test
    fun `Given success, When rename confirmed, Then dispatch CAS actions`() = runTest {
        assertTagEditTaskProcess(
            action = HomeAction.OnTagRenameConfirmClicked,
            buildTagEditStateMachine = {
                mockk { every { tryUpdateName(current.tagEditState, current.tags) } returns tagEditTaskStub }
            }
        )
    }

    @Test
    fun `Given success, When replace confirmed, Then dispatch CAS actions`() = runTest {
        assertTagEditTaskProcess(
            action = HomeAction.OnTagReplaceConfirmClicked,
            buildTagEditStateMachine = {
                mockk { every { merge(current.tagEditState) } returns tagEditTaskStub }
            }
        )
    }

    @Test
    fun `Given success, When replace canceled, Then update by stateMachine result`() = runTest {
        assertTagEditStateTransform(
            action = HomeAction.OnTagReplaceCancelClicked,
            buildStateMachine = {
                mockk { every { cancelMerge(current.tagEditState) } returns nextTagEditState }
            }
        )
    }

    @Test
    fun `Given success state, When delete requested, Then dispatch CAS actions`() = runTest {
        assertTagEditTaskProcess(
            action = HomeAction.OnTagDeleteRequestClicked,
            buildTagEditStateMachine = {
                mockk { every { startDelete(current.tagEditState) } returns tagEditTaskStub }
            }
        )
    }



    @Test
    fun `Given success, When delete confirmed, Then dispatch CAS actions`() = runTest {
        assertTagEditTaskProcess(
            action = HomeAction.OnTagDeleteConfirmClicked,
            buildTagEditStateMachine = {
                mockk { every { delete(current.tagEditState) } returns tagEditTaskStub }
            }
        )
    }

    @Test
    fun `Given success with not none tagEditState, When tag Edit cancel clicked, Then update to none`() = runTest {
        genHomeReduce()
            .transitionScenario()
            .initState(
                genHomeUiStateSuccess(
                    tagEditState = genTagEditStateExcludeTypeOf<TagEditState.None>()
                )
            )
            .action(HomeAction.OnTagEditCancelClicked)
            .expectedStateFromInput { initState.copy(tagEditState = TagEditState.None) }
            .verify()
    }
}

private fun genHomeReduce(environment: HomeEnvironment = genHomeEnvironment()): HomeReduce = HomeReduce(environment)

private data class TagEditTransformAssertParam(
    val current: HomeUiState.Success,
    val nextTagEditState: TagEditState
)

private suspend fun assertTagEditStateTransform(
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

private data class TagEditTaskProcessAssertParam(
    val current: HomeUiState.Success,
    val tagEditTaskStub: TagEditTask
)

private suspend fun TestScope.assertTagEditTaskProcess(
    action: HomeAction,
    buildTagEditStateMachine: TagEditTaskProcessAssertParam.() -> TagEditStateMachine
) {
    assertTagEditTaskProcessSuccess(action, buildTagEditStateMachine)
    assertTagEditTaskProcessFailure(action, buildTagEditStateMachine)
}

private suspend fun assertTagEditTaskProcessSuccess(
    action: HomeAction,
    buildTagEditStateMachine: TagEditTaskProcessAssertParam.() -> TagEditStateMachine
) {
    // before
    mockkStatic(TagEditTask::processAsFlow)

    // test
    val initState = genHomeUiStateSuccess()
    val currentTagEditState = genTagEditStateTypeOf<TagEditState.None>()
    val nextTagEditState = genTagEditStateExcludeTypeOf<TagEditState.None>()
    val tagEditTaskStub: TagEditTask = mockk {
        every { processAsFlow() } returns flowOf(
            Result.Success(TagEditStateTransition(currentTagEditState, nextTagEditState))
        )
    }
    val tagEditStateMachine = buildTagEditStateMachine(
        TagEditTaskProcessAssertParam(initState, tagEditTaskStub)
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
                HomeAction.CompareAndSetTagEditState(currentTagEditState, nextTagEditState)
            )
        )
    )

    // after
    unmockkStatic(TagEditTask::processAsFlow)
}

private suspend fun TestScope.assertTagEditTaskProcessFailure(
    action: HomeAction,
    buildTagEditStateMachine: TagEditTaskProcessAssertParam.() -> TagEditStateMachine
) {
    // before
    mockkStatic(TagEditTask::processAsFlow)

    // test
    val initState = genHomeUiStateSuccess()
    val expectedThrowable = Throwable()
    val userMessageExceptionSourceStub = genUserMessageExceptionSource()
    val expectedUserMessageException = UserMessageException(
        origin = expectedThrowable,
        source = userMessageExceptionSourceStub
    )
    val tagEditTaskStub: TagEditTask = mockk {
        every { processAsFlow() } returns flowOf(Result.Failure(expectedThrowable))
    }
    val tagEditStateMachine = buildTagEditStateMachine(
        TagEditTaskProcessAssertParam(initState, tagEditTaskStub)
    )
    val userMessageFactory: UserMessageFactory = mockk {
        every { createExceptionSource() } returns userMessageExceptionSourceStub
    }
    val environment = genHomeEnvironment(
        tagEditStateMachine = tagEditStateMachine,
        userMessageFactory = userMessageFactory
    )
    lateinit var actualThrowable: UserMessageException
    genHomeReduce(environment = environment)
        .effectScenario()
        .initState(initState)
        .action(action)
        .launchIn(coroutineScope = this + CoroutineExceptionHandler { _, t ->
            actualThrowable = t as UserMessageException
        })
        .join()
    assertThat(actualThrowable.origin, sameInstance(expectedUserMessageException.origin))
    assertThat(actualThrowable.userMessage, equalTo(expectedUserMessageException.userMessage))

    // after
    unmockkStatic(TagEditTask::processAsFlow)
}