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

import androidx.paging.PagingData
import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.domain.common.schedule.genSchedules
import com.nlab.reminder.domain.feature.schedule.all.AllScheduleReport
import com.nlab.reminder.test.genBoolean

/**
 * @author Doohyun
 */
fun genAllScheduleReport(
    doingSchedules: List<Schedule> = genSchedules(isComplete = false),
    doneSchedules: PagingData<Schedule> = PagingData.from(genSchedules(isComplete = true)),
    isDoneScheduleShown: Boolean = genBoolean()
): AllScheduleReport = AllScheduleReport(doingSchedules, doneSchedules, isDoneScheduleShown)