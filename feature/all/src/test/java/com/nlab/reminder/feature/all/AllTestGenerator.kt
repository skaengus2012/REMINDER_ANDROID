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

import com.nlab.reminder.core.component.currenttime.GetCurrentTimeSnapshotStreamUseCase
import com.nlab.reminder.core.component.schedule.UpdateScheduleCompletionUseCase
import com.nlab.reminder.core.component.schedulelist.content.EditScheduleListResourceUseCase
import com.nlab.reminder.core.component.schedulelist.content.UserSelectedSchedulesStore
import com.nlab.reminder.core.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.core.data.repository.ScheduleRepository
import io.mockk.mockk

/**
 * @author Doohyun
 */
internal fun genAllEnvironment(
    scheduleRepository: ScheduleRepository = mockk(),
    completedScheduleShownRepository: CompletedScheduleShownRepository = mockk(),
    getUserScheduleListResourceReportFlow: GetUserScheduleListResourceReportFlowUseCase = mockk(),
    getCurrentTimeSnapshotStream: GetCurrentTimeSnapshotStreamUseCase = mockk(),
    updateScheduleCompletion: UpdateScheduleCompletionUseCase = mockk(),
    userSelectedSchedulesStore: UserSelectedSchedulesStore = mockk(),
    editScheduleListResource: EditScheduleListResourceUseCase = mockk()
): AllEnvironment = AllEnvironment(
    scheduleRepository = scheduleRepository,
    completedScheduleShownRepository = completedScheduleShownRepository,
    getUserScheduleListResourceReportFlow = getUserScheduleListResourceReportFlow,
    getCurrentTimeSnapshotStream = getCurrentTimeSnapshotStream,
    updateScheduleCompletion = updateScheduleCompletion,
    userSelectedSchedulesStore = userSelectedSchedulesStore,
    editScheduleListResource = editScheduleListResource
)