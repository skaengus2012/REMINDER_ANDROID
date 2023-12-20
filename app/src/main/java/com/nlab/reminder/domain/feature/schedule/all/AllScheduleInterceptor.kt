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

import com.nlab.reminder.core.kotlin.util.getOrThrow
import com.nlab.reminder.domain.common.data.repository.CompletedScheduleShownAllData
import com.nlab.reminder.domain.common.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.domain.common.data.repository.ScheduleDeleteRequest
import com.nlab.reminder.domain.common.data.repository.ScheduleRepository
import com.nlab.statekit.middleware.interceptor.Interceptor
import com.nlab.statekit.util.buildDslInterceptor
import javax.inject.Inject

/**
 * @author thalys
 */
internal class AllScheduleInterceptor @Inject constructor(
    scheduleRepository: ScheduleRepository,
    @CompletedScheduleShownAllData completedScheduleShownRepository: CompletedScheduleShownRepository
) : Interceptor<AllScheduleAction, AllScheduleUiState> by buildDslInterceptor(defineDSL = {
    state<AllScheduleUiState.Loaded> {
        action<AllScheduleAction.OnCompletedScheduleVisibilityUpdateClicked> { (action) ->
            completedScheduleShownRepository
                .setShown(isShown = action.isVisible)
                .getOrThrow()
        }

        // delete
        action<AllScheduleAction.OnScheduleDeleteClicked> { (action) ->
            scheduleRepository.delete(ScheduleDeleteRequest.ById(action.id))
                .getOrThrow()
        }
        action<AllScheduleAction.OnSelectedSchedulesDeleteClicked> { (action) ->
            scheduleRepository.delete(ScheduleDeleteRequest.ByIds(action.ids))
                .getOrThrow()
        }
        action<AllScheduleAction.OnCompletedScheduleDeleteClicked> {
            scheduleRepository.delete(ScheduleDeleteRequest.ByComplete(isComplete = true))
                .getOrThrow()
        }
    }
})