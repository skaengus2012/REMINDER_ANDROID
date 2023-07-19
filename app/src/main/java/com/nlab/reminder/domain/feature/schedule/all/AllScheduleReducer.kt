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

package com.nlab.reminder.domain.feature.schedule.all

import com.nlab.statekit.Reducer
import com.nlab.statekit.util.buildDslReducer
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

private typealias DomainReducer = Reducer<AllScheduleAction, AllScheduleUiState>

/**
 * @author Doohyun
 */
internal class AllScheduleReducer @Inject constructor() : DomainReducer by buildDslReducer(defineDSL = {
    action<AllScheduleAction.ScheduleLoaded> {
        state<AllScheduleUiState.Idle> { (action) ->
            AllScheduleUiState.Loaded(
                schedules = action.schedules.toImmutableList(),
                isCompletedScheduleShown = action.isCompletedScheduleShown,
                isSelectionMode = false
            )
        }
        state<AllScheduleUiState.Loaded> { (action, before) ->
            before.copy(
                schedules = action.schedules.toImmutableList(),
                isCompletedScheduleShown = action.isCompletedScheduleShown
            )
        }
    }
    state<AllScheduleUiState.Loaded> {
        action<AllScheduleAction.OnSelectionModeUpdateClicked> { (action, before) ->
            before.copy(isSelectionMode = action.isSelectionMode)
        }
    }
})