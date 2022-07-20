/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.domain.feature.end.all

import androidx.paging.PagingData
import com.nlab.reminder.core.state.StateMachine
import com.nlab.reminder.core.state.util.StateMachine
import kotlinx.coroutines.CoroutineScope

typealias AllEndStateMachine = StateMachine<AllEndAction, AllEndState>

/**
 * @author Doohyun
 */
fun AllEndStateMachine(
    scope: CoroutineScope,
    initState: AllEndState
): AllEndStateMachine = StateMachine(scope, initState) {
    updateTo { (action, oldState) ->
        when (action) {
            is AllEndAction.Fetch -> {
                if (oldState is AllEndState.Init) AllEndState.Loading
                else oldState
            }
            is AllEndAction.DoingScheduleLoaded -> oldState.updateWhenLoaded { curLoaded ->
                curLoaded.copy(doingSchedules = action.doingSchedules)
            }
            is AllEndAction.DoneScheduleLoaded -> oldState.updateWhenLoaded { curLoaded ->
                curLoaded.copy(doneSchedules = action.doneSchedule)
            }
            is AllEndAction.DoneScheduleShownChanged -> oldState.updateWhenLoaded { curLoaded ->
                curLoaded.copy(isDoneScheduleShown = action.isShow)
            }
        }
    }
}

private fun AllEndState.updateWhenLoaded(transform: (AllEndState.Loaded) -> AllEndState): AllEndState =
    if (this is AllEndState.Init) this
    else transform(
        if (this is AllEndState.Loaded) this else AllEndState.Loaded(
            doingSchedules = emptyList(),
            doneSchedules = PagingData.empty(),
            isDoneScheduleShown = false
        )
    )
