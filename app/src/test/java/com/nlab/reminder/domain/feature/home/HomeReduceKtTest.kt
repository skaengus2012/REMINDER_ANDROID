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

import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.genTags
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import com.nlab.statekit.test.reduce.transitionScenario
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock

/**
 * @author Thalys
 */
class HomeReduceKtTest {
    @Test
    fun `Given loading, When state synced, Then state changed to Success`() = runTest {
        genHomeReduce()
            .transitionScenario()
            .initState(HomeUiState.Loading)
            .action(genStateSyncedAction())
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
}

private fun genHomeReduce(environment: HomeEnvironment = mock()): HomeReduce = HomeReduce(environment)

private fun genStateSyncedAction(
    todaySchedulesCount: NonNegativeLong = genNonNegativeLong(),
    timetableSchedulesCount: NonNegativeLong = genNonNegativeLong(),
    allSchedulesCount: NonNegativeLong = genNonNegativeLong(),
    tags: List<Tag> = genTags()
) = HomeAction.StateSynced(
    todaySchedulesCount = todaySchedulesCount,
    timetableSchedulesCount = timetableSchedulesCount,
    allSchedulesCount = allSchedulesCount,
    tags = tags
)