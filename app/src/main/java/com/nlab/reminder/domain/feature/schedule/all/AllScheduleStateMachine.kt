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

package com.nlab.reminder.domain.feature.schedule.all

import com.nlab.reminder.core.state.util.StateMachine
import com.nlab.reminder.domain.common.schedule.UpdateCompleteUseCase

typealias AllScheduleStateMachine = StateMachine<AllScheduleEvent, AllScheduleState>

/**
 * @author Doohyun
 */
fun AllScheduleStateMachine(
    getAllScheduleReport: GetAllScheduleReportUseCase,
    updateScheduleComplete: UpdateCompleteUseCase
): AllScheduleStateMachine = StateMachine {
    update { (event, state) ->
        when (event) {
            is AllScheduleEvent.Fetch -> {
                if (state is AllScheduleState.Init) AllScheduleState.Loading
                else state
            }
            is AllScheduleEvent.AllScheduleReportLoaded -> {
                if (state is AllScheduleState.Init) state
                else AllScheduleState.Loaded(event.allSchedulesReport)
            }
            else -> state
        }
    }

    sideEffectOn<AllScheduleEvent.Fetch, AllScheduleState.Init> {
        getAllScheduleReport().collect { send(AllScheduleEvent.AllScheduleReportLoaded(it)) }
    }

    sideEffectOn<AllScheduleEvent.OnScheduleCompleteUpdateClicked, AllScheduleState.Loaded> { (event) ->
        updateScheduleComplete(event.scheduleId, event.isComplete)
    }
}