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
import com.nlab.reminder.domain.common.schedule.ModifyScheduleCompleteUseCase

/**
 * @author Doohyun
 */
@Suppress("FunctionName")
fun AllScheduleStateMachine(
    getAllScheduleSnapshot: GetAllScheduleSnapshotUseCase,
    modifyScheduleComplete: ModifyScheduleCompleteUseCase
): StateMachine<AllScheduleEvent, AllScheduleState> = StateMachine {
    reduce {
        event<AllScheduleEvent.Fetch> {
            state<AllScheduleState.Init> { AllScheduleState.Loading }
        }
        event<AllScheduleEvent.OnAllScheduleSnapshotLoaded> {
            stateNot<AllScheduleState.Init> { (event) -> AllScheduleState.Loaded(event.allSchedulesReport) }
        }
    }

    handle {
        event<AllScheduleEvent.Fetch> {
            state<AllScheduleState.Init> {
                getAllScheduleSnapshot().collect { send(AllScheduleEvent.OnAllScheduleSnapshotLoaded(it)) }
            }
        }
        event<AllScheduleEvent.OnModifyScheduleCompleteClicked> {
            state<AllScheduleState.Loaded> { (event) -> modifyScheduleComplete(event.scheduleId, event.isComplete) }
        }
    }
}