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

import com.nlab.reminder.domain.common.schedule.UpdateScheduleCompleteUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
fun genAllScheduleStateMachineFactory(
    getAllScheduleReport: GetAllScheduleReportUseCase = mock(),
    updateScheduleComplete: UpdateScheduleCompleteUseCase = mock(),
    initState: AllScheduleState? = null
): AllScheduleStateMachineFactory =
    if (initState == null) AllScheduleStateMachineFactory(getAllScheduleReport, updateScheduleComplete)
    else AllScheduleStateMachineFactory(getAllScheduleReport, updateScheduleComplete, initState)

fun genStateMachine(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
    initState: AllScheduleState? = null,
    getAllScheduleReport: GetAllScheduleReportUseCase = mock { whenever(mock()) doReturn emptyFlow() },
    updateScheduleComplete: UpdateScheduleCompleteUseCase = mock()
): AllScheduleStateMachine =
    genAllScheduleStateMachineFactory(getAllScheduleReport, updateScheduleComplete, initState)
        .create(scope)

fun genAllScheduleViewModel(
    getAllScheduleReport: GetAllScheduleReportUseCase = mock(),
    updateScheduleComplete: UpdateScheduleCompleteUseCase = mock(),
    initState: AllScheduleState? = null
): AllScheduleViewModel = AllScheduleViewModel(
    genAllScheduleStateMachineFactory(getAllScheduleReport, updateScheduleComplete, initState)
)

fun genAllScheduleMockingViewModelComponent(
    state: MutableStateFlow<AllScheduleState> = MutableStateFlow(AllScheduleState.Init)
): Triple<AllScheduleViewModel, AllScheduleStateMachine, AllScheduleStateMachineFactory> {
    val stateMachine: AllScheduleStateMachine = mock { whenever(mock.state) doReturn state }
    val stateMachineFactory: AllScheduleStateMachineFactory = mock {
        whenever(
            mock.create(
                scope = any()
            )
        ) doReturn stateMachine
    }

    val viewModel = AllScheduleViewModel(stateMachineFactory)
    return Triple(viewModel, stateMachine, stateMachineFactory)
}