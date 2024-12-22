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

package com.nlab.reminder.domain.feature.home

import com.nlab.reminder.core.component.tag.edit.TagEditDelegate
import com.nlab.reminder.core.component.tag.edit.genTagEditState
import com.nlab.reminder.core.component.text.UiText
import com.nlab.reminder.core.component.text.genUiTexts
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.translation.StringIds
import com.nlab.statekit.test.reduce.effectScenario
import com.nlab.statekit.test.reduce.expectedStateToInit
import com.nlab.statekit.test.reduce.transitionScenario
import com.nlab.testkit.faker.genBothify
import com.nlab.testkit.faker.genInt
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.once
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.verification.VerificationMode

/**
 * @author Thalys
 */
class HomeReduceKtTest {
    @Test
    fun `Given loading, When state synced, Then state changed to Success`() = runTest {
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
                    interaction = HomeInteraction.Empty,
                    userMessages = emptyList()
                )
            }
            .verify()
    }

    @Test
    fun `Given success, When state synced, Then state changed by state synced action`() = runTest {
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
                    interaction = initState.interaction,
                    userMessages = initState.userMessages
                )
            }
            .verify()
    }

    @Test
    fun `Given loading, When sync tagEditState, Then tagEditDelegate invoke clearState conditionally`() = runTest {
        suspend fun testTagEditStateSyncedWhenLoading(
            action: HomeAction.TagEditStateSynced,
            mockVerifyMode: VerificationMode
        ) {
            val tagEditDelegate: TagEditDelegate = mock()
            genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
                .effectScenario()
                .initState(HomeUiState.Loading)
                .action(action)
                .launchAndJoin()
            verify(tagEditDelegate, mockVerifyMode).clearState()
        }

        testTagEditStateSyncedWhenLoading(
            action = HomeAction.TagEditStateSynced(genTagEditState()),
            mockVerifyMode = once()
        )
        testTagEditStateSyncedWhenLoading(
            action = HomeAction.TagEditStateSynced(null),
            mockVerifyMode = never()
        )
    }

    @Test
    fun `Given loading, When sync tagEditStep as null, Then tagEditDelegate never invoked clearState`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock()
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .effectScenario()
            .initState(HomeUiState.Loading)
            .action(HomeAction.TagEditStateSynced(null))
            .launchAndJoin()
        verify(tagEditDelegate, never()).clearState()
    }

    @Test
    fun `Given success with empty or tag edit interaction, When sync tagEditStep as null, Then interaction changed to empty`() = runTest {
        suspend fun testInteractionToEmpty(initInteraction: HomeInteraction) {
            genHomeReduce()
                .transitionScenario()
                .initState(genHomeUiStateSuccess(interaction = initInteraction))
                .action(HomeAction.TagEditStateSynced(null))
                .expectedStateFromInput { initState.copy(interaction = HomeInteraction.Empty) }
                .verify()
        }
        testInteractionToEmpty(initInteraction = HomeInteraction.Empty)
        testInteractionToEmpty(initInteraction = HomeInteraction.TagEdit(genTagEditState()))
    }

    @Test
    fun `Given success with empty or tag edit interaction, When sync exist tagEditStep, Then tagEditDelegate never invoked clearState`() = runTest {
        suspend fun testNeverCallClearState(initInteraction: HomeInteraction) {
            val tagEditDelegate: TagEditDelegate = mock()
            genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
                .effectScenario()
                .initState(genHomeUiStateSuccess(interaction = initInteraction))
                .action(HomeAction.TagEditStateSynced(genTagEditState()))
                .launchAndJoin()
            verify(tagEditDelegate, never()).clearState()
        }

        testNeverCallClearState(initInteraction = HomeInteraction.Empty)
        testNeverCallClearState(initInteraction = HomeInteraction.TagEdit(genTagEditState()))
    }

    @Test
    fun `Given success with empty or tag edit interaction, When sync exist tagEditStep, Then interaction changed with tagEditStep`() = runTest {
        suspend fun testInteractionToTagEdit(initInteraction: HomeInteraction) {
            genHomeReduce()
                .transitionScenario()
                .initState(genHomeUiStateSuccess(interaction = initInteraction))
                .action(HomeAction.TagEditStateSynced(genTagEditState()))
                .expectedStateFromInput {
                    initState.copy(
                        interaction = HomeInteraction.TagEdit(state = action.state!!)
                    )
                }
                .verify()
        }
        testInteractionToTagEdit(initInteraction = HomeInteraction.Empty)
        testInteractionToTagEdit(initInteraction = HomeInteraction.TagEdit(genTagEditState()))
    }

    @Test
    fun `Given success with no interaction and user messages, When tag long clicked, Then user message filled if TagEditDelegate startEditing result fails`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock {
            whenever(mock.startEditing(any())) doReturn Result.Failure(IllegalStateException())
        }
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .transitionScenario()
            .initState(genHomeUiStateSuccess(interaction = HomeInteraction.Empty, userMessages = emptyList()))
            .action(HomeAction.OnTagLongClicked(genTag()))
            .expectedStateFromInput {
                initState.copy(userMessages = listOf(UiText(StringIds.tag_not_found)))
            }
            .verify(shouldVerifyWithEffect = true)
    }

    @Test
    fun `Given success with tagEdit interaction, When tag rename request clicked, Then tagEditDelegate called startRename`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock()
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .effectScenario()
            .initState(genHomeUiStateSuccess(interaction = HomeInteraction.TagEdit(genTagEditState())))
            .action(HomeAction.OnTagRenameRequestClicked)
            .launchAndJoin()
        verify(tagEditDelegate, once()).startRename()
    }

    @Test
    fun `Given success with tagEdit interaction, When tag rename input ready, Then tagEditDelegate called readyRenameInput`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock()
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .effectScenario()
            .initState(genHomeUiStateSuccess(interaction = HomeInteraction.TagEdit(genTagEditState())))
            .action(HomeAction.OnTagRenameInputReady)
            .launchAndJoin()
        verify(tagEditDelegate, once()).readyRenameInput()
    }

    @Test
    fun `Given success with tagEdit interaction, When tag rename inputted, Then tagEditDelegate called changeRenameText`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock()
        val input = genBothify()
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .effectScenario()
            .initState(genHomeUiStateSuccess(interaction = HomeInteraction.TagEdit(genTagEditState())))
            .action(HomeAction.OnTagRenameInputted(input))
            .launchAndJoin()
        verify(tagEditDelegate, once()).changeRenameText(input)
    }

    @Test
    fun `Given success with tagEdit interaction and no user messages, When tag rename confirmed, Then user message filled if TagEditDelegate tryUpdateTagRename result fails`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock {
            whenever(mock.tryUpdateTagName(any())) doReturn Result.Failure(IllegalStateException())
        }
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .transitionScenario()
            .initState(
                genHomeUiStateSuccess(
                    interaction = HomeInteraction.TagEdit(genTagEditState()),
                    userMessages = emptyList()
                )
            )
            .action(HomeAction.OnTagRenameConfirmClicked)
            .expectedStateFromInput {
                initState.copy(userMessages = listOf(UiText(StringIds.unknown_error)))
            }
            .verify(shouldVerifyWithEffect = true)
    }

    @Test
    fun `Given success with tagEdit interaction and no user messages, When tag replace confirm clicked, Then user message filled if TagEditDelegate mergeTag result fails`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock {
            whenever(mock.mergeTag()) doReturn Result.Failure(IllegalStateException())
        }
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .transitionScenario()
            .initState(
                genHomeUiStateSuccess(
                    interaction = HomeInteraction.TagEdit(genTagEditState()),
                    userMessages = emptyList()
                )
            )
            .action(HomeAction.OnTagReplaceConfirmClicked)
            .expectedStateFromInput {
                initState.copy(userMessages = listOf(UiText(StringIds.unknown_error)))
            }
            .verify(shouldVerifyWithEffect = true)
    }

    @Test
    fun `Given success with tagEdit interaction, When tag rename cancel clicked, Then tagEditDelegate called cancelRename`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock()
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .effectScenario()
            .initState(genHomeUiStateSuccess(interaction = HomeInteraction.TagEdit(genTagEditState())))
            .action(HomeAction.OnTagReplaceCancelClicked)
            .launchAndJoin()
        verify(tagEditDelegate, once()).cancelMergeTag()
    }

    @Test
    fun `Given success with tagEdit interaction, When tag delete request clicked, Then tagEditDelegate called startDelete`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock()
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .effectScenario()
            .initState(genHomeUiStateSuccess(interaction = HomeInteraction.TagEdit(genTagEditState())))
            .action(HomeAction.OnTagDeleteRequestClicked)
            .launchAndJoin()
        verify(tagEditDelegate, once()).startDelete()
    }

    @Test
    fun `Given success with tagEdit interaction and no user messages, When tag delete confirm clicked, Then user message filled if TagEditDelegate deleteTag result fails`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock {
            whenever(mock.deleteTag()) doReturn Result.Failure(IllegalStateException())
        }
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .transitionScenario()
            .initState(
                genHomeUiStateSuccess(
                    interaction = HomeInteraction.TagEdit(genTagEditState()),
                    userMessages = emptyList()
                )
            )
            .action(HomeAction.OnTagDeleteConfirmClicked)
            .expectedStateFromInput {
                initState.copy(userMessages = listOf(UiText(StringIds.unknown_error)))
            }
            .verify(shouldVerifyWithEffect = true)
    }

    @Test
    fun `Given success with empty user messages, When user message posted, Then user message added to state`() = runTest {
        genHomeReduce()
            .transitionScenario()
            .initState(genHomeUiStateSuccess(userMessages = emptyList()))
            .action(HomeAction.UserMessagePosted(UiText(genBothify())))
            .expectedStateFromInput {
                initState.copy(
                    userMessages = listOf(action.message)
                )
            }
            .verify()
    }

    @Test
    fun `Given success with 2 or more user messages, When user message shown, Then user message removed`() = runTest {
        val userMessages = genUiTexts(genInt(min = 2, max = 10))

        genHomeReduce()
            .transitionScenario()
            .initState(genHomeUiStateSuccess(userMessages = userMessages))
            .action(HomeAction.UserMessageShown(userMessages[1]))
            .expectedStateFromInput {
                initState.copy(
                    userMessages = initState.userMessages - action.message
                )
            }
            .verify()
    }

    @Test
    fun `Given success with interaction exclude empty, When interacted, Then interaction changed to empty`() = runTest {
        genHomeReduce()
            .transitionScenario()
            .initState(
                genHomeUiStateSuccess(interaction = genHomeInteractionWithExcludeTypes(HomeInteraction.Empty::class))
            )
            .action(HomeAction.Interacted)
            .expectedStateFromInput { initState.copy(interaction = HomeInteraction.Empty) }
            .verify()
    }

    @Test
    fun `Given success with tagEdit interaction, When interacted, Then tagEditDelegate called clearState`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock()
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate = tagEditDelegate))
            .effectScenario()
            .initState(genHomeUiStateSuccess(interaction = HomeInteraction.TagEdit(genTagEditState())))
            .action(HomeAction.Interacted)
            .launchAndJoin()
        verify(tagEditDelegate, once()).clearState()
    }

    @Test
    fun `Given success with not tagEdit interaction, When interacted, Then tagEditDelegate never called clearState`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock()
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate = tagEditDelegate))
            .effectScenario()
            .initState(genHomeUiStateSuccess(
                interaction = genHomeInteractionWithExcludeTypes(HomeInteraction.TagEdit::class))
            )
            .action(HomeAction.Interacted)
            .launchAndJoin()
        verify(tagEditDelegate, never()).clearState()
    }
}

private fun genHomeReduce(environment: HomeEnvironment = genHomeEnvironment()): HomeReduce = HomeReduce(environment)