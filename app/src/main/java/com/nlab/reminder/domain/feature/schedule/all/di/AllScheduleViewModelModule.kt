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

package com.nlab.reminder.domain.feature.schedule.all.di

import com.nlab.reminder.core.effect.SideEffectHandle
import com.nlab.reminder.core.state.StateContainer
import com.nlab.reminder.core.state.asContainer
import com.nlab.reminder.domain.common.schedule.*
import com.nlab.reminder.domain.common.schedule.visibleconfig.CompletedScheduleShownRepository
import com.nlab.reminder.domain.common.schedule.impl.*
import com.nlab.reminder.domain.common.schedule.selection.SelectionModeRepository
import com.nlab.reminder.domain.feature.schedule.all.*
import com.nlab.reminder.domain.feature.schedule.all.impl.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.CoroutineScope

/**
 * @author Doohyun
 */
@Module
@InstallIn(ViewModelComponent::class)
class AllScheduleViewModelModule {
    @Provides
    fun provideStateMachineProvider(
        scheduleRepository: ScheduleRepository,
        scheduleUiStateFlowFactory: ScheduleUiStateFlowFactory,
        modifyScheduleCompleteUseCase: ModifyScheduleCompleteUseCase,
        selectionModeRepository: SelectionModeRepository,
        @AllScheduleScope completedScheduleShownRepository: CompletedScheduleShownRepository
    ): AllScheduleStateContainerFactory = object : AllScheduleStateContainerFactory {
        override fun create(
            scope: CoroutineScope,
            sideEffectHandle: SideEffectHandle<AllScheduleSideEffect>
        ): StateContainer<AllScheduleEvent, AllScheduleState> {
            val stateMachine = AllScheduleStateMachine(
                sideEffectHandle,
                DefaultGetAllScheduleSnapshotUseCase(
                    scheduleRepository,
                    scheduleUiStateFlowFactory,
                    completedScheduleShownRepository
                ),
                modifyScheduleCompleteUseCase,
                completedScheduleShownRepository,
                scheduleRepository,
                selectionModeRepository
            )
            return stateMachine.asContainer(scope, AllScheduleState.Init, fetchEvent = AllScheduleEvent.Fetch)
        }
    }
}