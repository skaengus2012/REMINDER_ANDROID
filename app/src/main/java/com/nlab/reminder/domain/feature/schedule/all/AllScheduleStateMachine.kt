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

import com.nlab.reminder.core.state.StateMachine
import com.nlab.reminder.core.state.util.StateMachine
import com.nlab.reminder.domain.common.schedule.UpdateScheduleCompleteUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias AllScheduleStateMachine = StateMachine<AllScheduleAction, AllScheduleState>

/**
 * @author Doohyun
 */
fun AllScheduleStateMachine(
    scope: CoroutineScope,
    initState: AllScheduleState,
    getAllScheduleReport: GetAllScheduleReportUseCase,
    updateScheduleComplete: UpdateScheduleCompleteUseCase
): AllScheduleStateMachine = StateMachine(scope, initState) {
    updateTo { (action, oldState) ->
        when (action) {
            is AllScheduleAction.Fetch -> {
                if (oldState is AllScheduleState.Init) AllScheduleState.Loading
                else oldState
            }
            is AllScheduleAction.AllScheduleReportLoaded -> {
                if (oldState is AllScheduleState.Init) oldState
                else AllScheduleState.Loaded(action.allSchedulesReport)
            }
            else -> oldState
        }
    }

    sideEffectOn<AllScheduleAction.Fetch, AllScheduleState.Init> {
        scope.launch { getAllScheduleReport().collect { send(AllScheduleAction.AllScheduleReportLoaded(it)) } }
    }

    sideEffectOn<AllScheduleAction.OnScheduleCompleteUpdateClicked, AllScheduleState.Loaded> { (action) ->
        scope.launch {


            // 1. schedule 을 local 에서 업데이트. 이 때 txId 발급 필요.
            // 2. 딜레이
            // 3. txId 비교하여 schedule 처리할것..
            updateScheduleComplete(action.scheduleId, action.isComplete)
        }
    }
}