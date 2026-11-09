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
import com.nlab.reminder.core.component.tag.edit.genGetTagEditTaskTransitionFlow
import com.nlab.reminder.core.component.tag.edit.genTagEditStateExcludeTypeOf
import com.nlab.reminder.core.component.tag.edit.genTagEditStateTypeOf
import com.nlab.reminder.core.component.usermessage.UserMessageException
import com.nlab.reminder.core.component.usermessage.genUserMessageExceptionSource
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.data.model.genTags
import com.nlab.statekit.test.reduce.LaunchedTrace
import com.nlab.statekit.test.reduce.expectedNotChanged
import com.nlab.statekit.test.reduce.test
import com.nlab.testkit.faker.genBothify
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
class HomeReduceKtTest {
    @Test
    fun `Given loading, When stateSynced received, Then become Success`() = runTest {
        genHomeReduce().test()
            .givenCurrent(HomeUiState.Loading)
            .actionToDispatch(genHomeActionStateSynced())
            .transitionScenario()
            .expectedNextState {
                HomeUiState.Success(
                    todayScheduleCount = action.todaySchedulesCount,
                    timetableScheduleCount = action.timetableSchedulesCount,
                    allScheduleCount = action.allSchedulesCount,
                    tags = action.sortedTags,
                    tagEditState = TagEditState.None
                )
            }
    }

    @Test
    fun `Given success, When stateSynced received, Then update fields from action`() = runTest {
        genHomeReduce().test()
            .givenCurrent(genHomeUiStateSuccess())
            .actionToDispatch(genHomeActionStateSynced())
            .transitionScenario()
            .expectedNextState {
                HomeUiState.Success(
                    todayScheduleCount = action.todaySchedulesCount,
                    timetableScheduleCount = action.timetableSchedulesCount,
                    allScheduleCount = action.allSchedulesCount,
                    tags = action.sortedTags,
                    tagEditState = current.tagEditState,
                )
            }
    }

    @Test
    fun `Given success, tagEditState matches expected, When CAS invoked, Then update state`() = runTest {
        val currentTagEditState = genTagEditStateTypeOf<TagEditState.None>()
        val nextTagEditState = genTagEditStateExcludeTypeOf<TagEditState.AwaitTaskSelection>()
        genHomeReduce().test()
            .givenCurrent(genHomeUiStateSuccess(tagEditState = currentTagEditState))
            .actionToDispatch(
                HomeAction.CompareAndSetTagEditState(
                    expectedState = currentTagEditState,
                    newState = nextTagEditState
                )
            )
            .transitionScenario()
            .expectedNextState { current.copy(tagEditState = nextTagEditState) }
    }

    @Test
    fun `Given success, tagEditState does not match expected, When CAS invoked, Then don't update state`() = runTest {
        val currentTagEditState = genTagEditStateTypeOf<TagEditState.Rename>()
        genHomeReduce().test()
            .givenCurrent(genHomeUiStateSuccess(tagEditState = currentTagEditState))
            .actionToDispatch(
                HomeAction.CompareAndSetTagEditState(
                    expectedState = TagEditState.None,
                    newState = TagEditState.None
                )
            )
            .transitionScenario()
            .expectedNotChanged()
    }

    @Test
    fun `Given success, When tag long clicked, Then update by stateMachine result`() = runTest {
        val (currentTagEditState, nextTagEditState) = genCurrentAndNextTagEditState()
        val tag = genTag()
        genHomeReduce(
            environment = genHomeEnvironment(tagEditStateMachine = mockk {
                every { startEditing(currentTagEditState, tag) } returns nextTagEditState
            })
        ).test()
            .givenCurrent(genHomeUiStateSuccess(tagEditState = genTagEditStateTypeOf<TagEditState.None>()))
            .actionToDispatch(HomeAction.TagLongClicked(tag))
            .transitionScenario()
            .expectedNextState { current.copy(tagEditState = nextTagEditState) }
    }

    @Test
    fun `Given success, rename starting success, When start rename, Then update state`() = runTest {
        val (currentTagEditState, nextTagEditState) = genCurrentAndNextTagEditState()
        val task = mockk<TagEditTask>()
        genHomeReduce(
            environment = genHomeEnvironment(
                tagEditStateMachine = mockk {
                    every { startRename(currentTagEditState) } returns task
                },
                getTagEditTaskTransitionFlow = genGetTagEditTaskTransitionFlow(
                    task = task,
                    previous = currentTagEditState,
                    updated = nextTagEditState
                )
            )
        ).test()
            .givenCurrent(genHomeUiStateSuccess(tagEditState = currentTagEditState))
            .actionToDispatch(HomeAction.TagRenameRequestClicked)
            .transitionScenario(dispatchWithEffect = true)
            .expectedNextState { current.copy(tagEditState = nextTagEditState) }
    }

    @Test
    fun `Given success, rename starting failed, When start rename, Then throw user err`() = runTest {
        val userMessageExceptionSource = genUserMessageExceptionSource()
        val expectedUserMessageException = UserMessageException(
            source = userMessageExceptionSource,
            origin = Throwable()
        )
        val task = mockk<TagEditTask>()
        val traces = genHomeReduce(
            environment = genHomeEnvironment(
                tagEditStateMachine = mockk {
                    every { startRename(any()) } returns task
                },
                getTagEditTaskTransitionFlow = genGetTagEditTaskTransitionFlow(
                    task = task,
                    throwable = expectedUserMessageException
                ),
                userMessageFactory = mockk {
                    every { createExceptionSource() } returns userMessageExceptionSource
                }
            ))
            .test()
            .givenCurrent(genHomeUiStateSuccess())
            .actionToDispatch(HomeAction.TagRenameRequestClicked)
            .effectScenario()
            .launchAndGetTrace()

        val actualUserMessageException = traces
            .mapNotNull { it as? LaunchedTrace.Error }
            .firstNotNullOf { it.throwable as? UserMessageException }
        assertThat(
            actualUserMessageException.origin,
            sameInstance(expectedUserMessageException.origin)
        )
        assertThat(
            actualUserMessageException.userMessage,
            equalTo(expectedUserMessageException.userMessage)
        )
    }

    @Test
    fun `Given success, When rename input ready, Then update by stateMachine result`() = runTest {
        val (currentTagEditState, nextTagEditState) = genCurrentAndNextTagEditState()
        genHomeReduce(
            environment = genHomeEnvironment(tagEditStateMachine = mockk {
                every { readyRenameInput(currentTagEditState) } returns nextTagEditState
            })
        ).test()
            .givenCurrent(genHomeUiStateSuccess(tagEditState = currentTagEditState))
            .actionToDispatch(HomeAction.TagRenameInputReady)
            .transitionScenario()
            .expectedNextState { current.copy(tagEditState = nextTagEditState) }
    }

    @Test
    fun `Given success, When rename inputted, Then update by stateMachine result`() = runTest {
        val (currentTagEditState, nextTagEditState) = genCurrentAndNextTagEditState()
        val input = genBothify()
        val tagEditStateMachine: TagEditStateMachine = mockk {
            every { changeRenameText(currentTagEditState, input) } returns nextTagEditState
        }
        genHomeReduce(environment = genHomeEnvironment(tagEditStateMachine = tagEditStateMachine)).test()
            .givenCurrent(genHomeUiStateSuccess(tagEditState = currentTagEditState))
            .actionToDispatch(HomeAction.TagRenameInputted(input))
            .transitionScenario()
            .expectedNextState { current.copy(tagEditState = nextTagEditState) }
    }

    @Test
    fun `Given success, update name success, When confirm rename, Then update state`() = runTest {
        val (currentTagEditState, nextTagEditState) = genCurrentAndNextTagEditState()
        val compareTags = genTags().toList()
        val task = mockk<TagEditTask>()
        genHomeReduce(
            environment = genHomeEnvironment(
                tagEditStateMachine = mockk {
                    every { tryUpdateName(currentTagEditState, compareTags) } returns task
                },
                getTagEditTaskTransitionFlow = genGetTagEditTaskTransitionFlow(
                    task = task,
                    previous = currentTagEditState,
                    updated = nextTagEditState
                )
            )
        ).test()
            .givenCurrent(genHomeUiStateSuccess(tags = compareTags, tagEditState = currentTagEditState))
            .actionToDispatch(HomeAction.TagRenameConfirmClicked)
            .transitionScenario(dispatchWithEffect = true)
            .expectedNextState { current.copy(tagEditState = nextTagEditState) }
    }

    @Test
    fun `Given success, update name failed, When confirm rename, Then throw user err`() = runTest {
        val userMessageExceptionSource = genUserMessageExceptionSource()
        val expectedUserMessageException = UserMessageException(
            source = userMessageExceptionSource,
            origin = Throwable()
        )
        val task = mockk<TagEditTask>()
        val traces = genHomeReduce(
            environment = genHomeEnvironment(
                tagEditStateMachine = mockk {
                    every { tryUpdateName(any(), any()) } returns task
                },
                getTagEditTaskTransitionFlow = genGetTagEditTaskTransitionFlow(
                    task = task,
                    throwable = expectedUserMessageException
                ),
                userMessageFactory = mockk {
                    every { createExceptionSource() } returns userMessageExceptionSource
                }
            ))
            .test()
            .givenCurrent(genHomeUiStateSuccess())
            .actionToDispatch(HomeAction.TagRenameConfirmClicked)
            .effectScenario()
            .launchAndGetTrace()
        assertUserMessageException(
            actualTraces = traces,
            expectedUserMessageException = expectedUserMessageException
        )
    }

    @Test
    fun `Given success, merge success, When replace confirmed, Then update state`() = runTest {
        val (currentTagEditState, nextTagEditState) = genCurrentAndNextTagEditState()
        val task = mockk<TagEditTask>()
        genHomeReduce(
            environment = genHomeEnvironment(
                tagEditStateMachine = mockk {
                    every { merge(currentTagEditState) } returns task
                },
                getTagEditTaskTransitionFlow = genGetTagEditTaskTransitionFlow(
                    task = task,
                    previous = currentTagEditState,
                    updated = nextTagEditState
                )
            )
        ).test()
            .givenCurrent(genHomeUiStateSuccess(tagEditState = currentTagEditState))
            .actionToDispatch(HomeAction.TagReplaceConfirmClicked)
            .transitionScenario(dispatchWithEffect = true)
            .expectedNextState { current.copy(tagEditState = nextTagEditState) }
    }

    @Test
    fun `Given success, merge failed, When replace confirmed, Then throw user err`() = runTest {
        val userMessageExceptionSource = genUserMessageExceptionSource()
        val expectedUserMessageException = UserMessageException(
            source = userMessageExceptionSource,
            origin = Throwable()
        )
        val task = mockk<TagEditTask>()
        val traces = genHomeReduce(
            environment = genHomeEnvironment(
                tagEditStateMachine = mockk {
                    every { merge(any()) } returns task
                },
                getTagEditTaskTransitionFlow = genGetTagEditTaskTransitionFlow(
                    task = task,
                    throwable = expectedUserMessageException
                ),
                userMessageFactory = mockk {
                    every { createExceptionSource() } returns userMessageExceptionSource
                }
            ))
            .test()
            .givenCurrent(genHomeUiStateSuccess())
            .actionToDispatch(HomeAction.TagReplaceConfirmClicked)
            .effectScenario()
            .launchAndGetTrace()
        assertUserMessageException(
            actualTraces = traces,
            expectedUserMessageException = expectedUserMessageException
        )
    }

    @Test
    fun `Given success, When replace canceled, Then update by stateMachine result`() = runTest {
        val (currentTagEditState, nextTagEditState) = genCurrentAndNextTagEditState()
        val tagEditStateMachine: TagEditStateMachine = mockk {
            every { cancelMerge(currentTagEditState) } returns nextTagEditState
        }
        genHomeReduce(environment = genHomeEnvironment(tagEditStateMachine = tagEditStateMachine)).test()
            .givenCurrent(genHomeUiStateSuccess(tagEditState = currentTagEditState))
            .actionToDispatch(HomeAction.TagReplaceCancelClicked)
            .transitionScenario()
            .expectedNextState { current.copy(tagEditState = nextTagEditState) }
    }

    @Test
    fun `Given success, start delete success, When start delete, Then update state`() = runTest {
        val (currentTagEditState, nextTagEditState) = genCurrentAndNextTagEditState()
        val task = mockk<TagEditTask>()
        genHomeReduce(
            environment = genHomeEnvironment(
                tagEditStateMachine = mockk {
                    every { startDelete(currentTagEditState) } returns task
                },
                getTagEditTaskTransitionFlow = genGetTagEditTaskTransitionFlow(
                    task = task,
                    previous = currentTagEditState,
                    updated = nextTagEditState
                )
            )
        ).test()
            .givenCurrent(genHomeUiStateSuccess(tagEditState = currentTagEditState))
            .actionToDispatch(HomeAction.TagDeleteRequestClicked)
            .transitionScenario(dispatchWithEffect = true)
            .expectedNextState { current.copy(tagEditState = nextTagEditState) }
    }

    @Test
    fun `Given success, start delete failed, When start delete, Then throw user err`() = runTest {
        val userMessageExceptionSource = genUserMessageExceptionSource()
        val expectedUserMessageException = UserMessageException(
            source = userMessageExceptionSource,
            origin = Throwable()
        )
        val task = mockk<TagEditTask>()
        val traces = genHomeReduce(
            environment = genHomeEnvironment(
                tagEditStateMachine = mockk {
                    every { startDelete(any()) } returns task
                },
                getTagEditTaskTransitionFlow = genGetTagEditTaskTransitionFlow(
                    task = task,
                    throwable = expectedUserMessageException
                ),
                userMessageFactory = mockk {
                    every { createExceptionSource() } returns userMessageExceptionSource
                }
            ))
            .test()
            .givenCurrent(genHomeUiStateSuccess())
            .actionToDispatch(HomeAction.TagDeleteRequestClicked)
            .effectScenario()
            .launchAndGetTrace()
        assertUserMessageException(
            actualTraces = traces,
            expectedUserMessageException = expectedUserMessageException,
        )
    }

    @Test
    fun `Given success, delete success, When confirm delete, Then update state`() = runTest {
        val (currentTagEditState, nextTagEditState) = genCurrentAndNextTagEditState()
        val task = mockk<TagEditTask>()
        genHomeReduce(
            environment = genHomeEnvironment(
                tagEditStateMachine = mockk {
                    every { delete(currentTagEditState) } returns task
                },
                getTagEditTaskTransitionFlow = genGetTagEditTaskTransitionFlow(
                    task = task,
                    previous = currentTagEditState,
                    updated = nextTagEditState
                )
            )
        ).test()
            .givenCurrent(genHomeUiStateSuccess(tagEditState = currentTagEditState))
            .actionToDispatch(HomeAction.TagDeleteConfirmClicked)
            .transitionScenario(dispatchWithEffect = true)
            .expectedNextState { current.copy(tagEditState = nextTagEditState) }
    }

    @Test
    fun `Given success, delete failed, When confirm delete, Then throw user err`() = runTest {
        val userMessageExceptionSource = genUserMessageExceptionSource()
        val expectedUserMessageException = UserMessageException(
            source = userMessageExceptionSource,
            origin = Throwable()
        )
        val task = mockk<TagEditTask>()
        val traces = genHomeReduce(
            environment = genHomeEnvironment(
                tagEditStateMachine = mockk {
                    every { delete(any()) } returns task
                },
                getTagEditTaskTransitionFlow = genGetTagEditTaskTransitionFlow(
                    task = task,
                    throwable = expectedUserMessageException
                ),
                userMessageFactory = mockk {
                    every { createExceptionSource() } returns userMessageExceptionSource
                }
            ))
            .test()
            .givenCurrent(genHomeUiStateSuccess())
            .actionToDispatch(HomeAction.TagDeleteConfirmClicked)
            .effectScenario()
            .launchAndGetTrace()
        assertUserMessageException(
            actualTraces = traces,
            expectedUserMessageException = expectedUserMessageException,
        )
    }

    @Test
    fun `Given success with not none tagEditState, When tag Edit cancel clicked, Then update to none`() = runTest {
        genHomeReduce().test()
            .givenCurrent(
                genHomeUiStateSuccess(
                    tagEditState = genTagEditStateExcludeTypeOf<TagEditState.None>()
                )
            )
            .actionToDispatch(HomeAction.TagEditCancelClicked)
            .transitionScenario()
            .expectedNextState { current.copy(tagEditState = TagEditState.None) }
    }
}

private fun genHomeReduce(environment: HomeEnvironment = genHomeEnvironment()): HomeReduce = HomeReduce(environment)

private fun genCurrentAndNextTagEditState(): Pair<TagEditState, TagEditState> {
    val currentTagEditState = genTagEditStateTypeOf<TagEditState.None>()
    val nextTagEditState = genTagEditStateExcludeTypeOf(currentTagEditState::class)
    return currentTagEditState to nextTagEditState
}

private fun assertUserMessageException(
    actualTraces: List<LaunchedTrace<*, *, *, *>>,
    expectedUserMessageException: UserMessageException,
) {
    val actualUserMessageException = actualTraces
        .mapNotNull { it as? LaunchedTrace.Error }
        .firstNotNullOf { it.throwable as? UserMessageException }
    assertThat(
        actualUserMessageException.origin,
        sameInstance(expectedUserMessageException.origin)
    )
    assertThat(
        actualUserMessageException.userMessage,
        equalTo(expectedUserMessageException.userMessage)
    )
}