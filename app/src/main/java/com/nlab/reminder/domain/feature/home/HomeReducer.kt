/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

import com.nlab.statekit.Reducer
import com.nlab.statekit.util.buildDslReducer
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

/**
 * @author Doohyun
 */
internal class HomeReducer @Inject constructor() : Reducer<HomeAction, HomeUiState> by buildDslReducer(
    defineDSL = {
        state<HomeUiState.Success> {
            action<HomeAction.PageShown> { (_, before) -> before.withPageShown() }
            action<HomeAction.OnTodayCategoryClicked> { (_, before) ->
                before.withPageShown(todayScheduleShow = true)
            }
            action<HomeAction.OnTimetableCategoryClicked> { (_, before) ->
                before.withPageShown(timetableScheduleShow = true)
            }
            action<HomeAction.OnAllCategoryClicked> { (_, before) ->
                before.withPageShown(allScheduleShow = true)
            }
        }

        action<HomeAction.SummaryLoaded> {
            state<HomeUiState.Loading> { (action) ->
                HomeUiState.Success(
                    todayScheduleCount = action.todayScheduleCount,
                    todayScheduleShown = false,
                    timetableScheduleCount = action.timetableScheduleCount,
                    timetableScheduleShown = false,
                    allScheduleCount = action.allScheduleCount,
                    allScheduleShown = false,
                    tags = action.tags.toImmutableList()
                )
            }
            state<HomeUiState.Success> { (action, before) ->
                before.copy(
                    todayScheduleCount = action.todayScheduleCount,
                    timetableScheduleCount = action.timetableScheduleCount,
                    allScheduleCount = action.allScheduleCount,
                    tags = action.tags.toImmutableList()
                )
            }
        }
    }
)