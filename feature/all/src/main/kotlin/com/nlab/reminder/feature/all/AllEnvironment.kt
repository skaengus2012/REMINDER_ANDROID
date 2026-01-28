/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.feature.all

import androidx.lifecycle.ViewModel
import com.nlab.reminder.core.component.currenttime.GetCurrentTimeSnapshotStreamUseCase
import com.nlab.reminder.core.component.schedule.UpdateScheduleCompletionUseCase
import com.nlab.reminder.core.component.schedulelist.content.EditScheduleListResourceUseCase
import com.nlab.reminder.core.component.schedulelist.content.UserSelectedSchedulesStore
import com.nlab.reminder.core.data.qualifiers.ScheduleData
import com.nlab.reminder.core.data.qualifiers.ScheduleDataOption.All
import com.nlab.reminder.core.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author Thalys
 */
@HiltViewModel
internal class AllEnvironment @Inject constructor(
    val scheduleRepository: ScheduleRepository,
    @param:ScheduleData(All) val completedScheduleShownRepository: CompletedScheduleShownRepository,
    val getUserScheduleListResourceReportFlow: GetUserScheduleListResourceReportFlowUseCase,
    val getCurrentTimeSnapshotStream: GetCurrentTimeSnapshotStreamUseCase,
    val updateScheduleCompletion: UpdateScheduleCompletionUseCase,
    val userSelectedSchedulesStore: UserSelectedSchedulesStore,
    val editScheduleListResource: EditScheduleListResourceUseCase
) : ViewModel()