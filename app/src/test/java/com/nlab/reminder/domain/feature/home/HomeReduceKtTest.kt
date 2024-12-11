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
import com.nlab.reminder.core.component.tag.edit.TagEditState
import com.nlab.reminder.core.component.tag.edit.genTagEditStateExcludeTypeOf
import com.nlab.statekit.test.reduce.effectScenario
import com.nlab.statekit.test.reduce.expectedStateToInit
import com.nlab.statekit.test.reduce.transitionScenario
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

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
                    tags = action.tags,
                    interaction = HomeInteraction.Empty,
                    userMessages = emptyList(),
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
                    tags = action.tags,
                    interaction = initState.interaction,
                    userMessages = initState.userMessages
                )
            }
            .verify()
    }

    @Test
    fun `Given loading, When empty tagEditeState synced, Then tagEditDelegate never invoked clearStep`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock()
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .effectScenario()
            .initState(HomeUiState.Loading)
            .action(HomeAction.TagEditStateSynced(TagEditState.Empty))
            .launchAndJoin()
        verify(tagEditDelegate, never()).clearState()
    }

    @Test
    fun `Given loading, When non-empty tagEditState synced, Then tagEditDelegate invoked clearStep`() = runTest {
        val tagEditDelegate: TagEditDelegate = mock()
        genHomeReduce(environment = genHomeEnvironment(tagEditDelegate))
            .effectScenario()
            .initState(HomeUiState.Loading)
            .action(HomeAction.TagEditStateSynced(genTagEditStateExcludeTypeOf<TagEditState.Empty>()))
            .launchAndJoin()
        verify(tagEditDelegate, once()).clearState()
    }

    @Test
    fun `Given success with not tag edit interaction, When empty tagEditStep synced, Then state never changed`() = runTest {
        genHomeReduce()
            .transitionScenario()
            .initState(
                genHomeUiStateSuccess(
                    interaction = genHomeInteractionWithExcludeTypes(HomeInteraction.TagEdit::class)
                )
            )
            .action(HomeAction.TagEditStateSynced(TagEditState.Empty))
            .expectedStateToInit()
            .verify()
    }

    @Test
    fun `Given success with tag edit interaction, When empty tagEditStep synced, Then state interaction changed to empty`() = runTest {
        genHomeReduce()
            .transitionScenario()
            .initState(
                genHomeUiStateSuccess(
                    interaction = HomeInteraction.TagEdit(
                        state = genTagEditStateExcludeTypeOf<TagEditState.Empty>()
                    )
                )
            )
            .action(HomeAction.TagEditStateSynced(TagEditState.Empty))
            .expectedStateFromInput { initState.copy(interaction = HomeInteraction.Empty) }
            .verify()
    }

    @Test
    fun `Given success with tag edit interaction, When non-empty tagEditStep synced, Then state interaction changed with tagEditStep`() = runTest {
        genHomeReduce()
            .transitionScenario()
            .initState(
                genHomeUiStateSuccess(
                    interaction = HomeInteraction.TagEdit(
                        state = genTagEditStateExcludeTypeOf<TagEditState.Empty>()
                    )
                )
            )
            .action(HomeAction.TagEditStateSynced(genTagEditStateExcludeTypeOf<TagEditState.Empty>()))
            .expectedStateFromInput {
                initState.copy(
                    interaction = HomeInteraction.TagEdit(state = action.state)
                )
            }
            .verify()
    }
}

private fun genHomeReduce(environment: HomeEnvironment = genHomeEnvironment()): HomeReduce = HomeReduce(environment)