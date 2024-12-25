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
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.kotlin.Result
import com.nlab.statekit.test.reduce.effectScenario
import com.nlab.statekit.test.reduce.launchAndJoin
import com.nlab.statekit.test.reduce.transitionScenario
import com.nlab.testkit.faker.genBothify
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
                    interaction = HomeInteraction.Empty
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
    fun `Given success with no interaction, When tag long clicked, Then TagEditDelegate invoke startEditing`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock()
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .effectScenario()
            .initState(genHomeUiStateSuccess(interaction = HomeInteraction.Empty))
            .action(HomeAction.OnTagLongClicked(genTag()))
            .launchAndJoin {
                verify(tagEditDelegate, once()).startEditing(action.tag)
            }
    }

    @Test
    fun `Given success with tagEdit interaction, When tag rename request clicked, Then tagEditDelegate called startRename`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock {
            whenever(mock.startRename()) doReturn Result.Success(Unit)
        }
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
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .effectScenario()
            .initState(genHomeUiStateSuccess(interaction = HomeInteraction.TagEdit(genTagEditState())))
            .action(HomeAction.OnTagRenameInputted(genBothify()))
            .launchAndJoin {
                verify(tagEditDelegate, once()).changeRenameText(action.text)
            }
    }

    @Test
    fun `Given success with tagEdit interaction, When tag rename confirmed, Then TagEditDelegate invoke tryUpdateTagRename`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock {
            whenever(mock.tryUpdateTagName(any())) doReturn Result.Success(Unit)
        }
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .effectScenario()
            .initState( genHomeUiStateSuccess(interaction = HomeInteraction.TagEdit(genTagEditState())))
            .action(HomeAction.OnTagRenameConfirmClicked)
            .launchAndJoin {
                verify(tagEditDelegate, once()).tryUpdateTagName(initState.tags)
            }
    }

    @Test
    fun `Given success with tagEdit interaction, When tag replace confirm clicked, Then TagEditDelegate invoke mergeTag`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock {
            whenever(mock.mergeTag()) doReturn Result.Success(Unit)
        }
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .effectScenario()
            .initState(genHomeUiStateSuccess(interaction = HomeInteraction.TagEdit(genTagEditState())))
            .action(HomeAction.OnTagReplaceConfirmClicked)
            .launchAndJoin {
                verify(tagEditDelegate, once()).mergeTag()
            }
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
        val tagEditDelegate: TagEditDelegate = mock {
            whenever(mock.startDelete()) doReturn Result.Success(Unit)
        }
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .effectScenario()
            .initState(genHomeUiStateSuccess(interaction = HomeInteraction.TagEdit(genTagEditState())))
            .action(HomeAction.OnTagDeleteRequestClicked)
            .launchAndJoin()
        verify(tagEditDelegate, once()).startDelete()
    }

    @Test
    fun `Given success with tagEdit interaction, When tag delete confirm clicked, Then TagEditDelegate invoked deleteTag`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock {
            whenever(mock.deleteTag()) doReturn Result.Success(Unit)
        }
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .effectScenario()
            .initState(genHomeUiStateSuccess(interaction = HomeInteraction.TagEdit(genTagEditState())))
            .action(HomeAction.OnTagDeleteConfirmClicked)
            .launchAndJoin {
                verify(tagEditDelegate, once()).deleteTag()
            }
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