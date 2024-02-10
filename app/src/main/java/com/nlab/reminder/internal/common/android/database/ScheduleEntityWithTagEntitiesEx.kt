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

package com.nlab.reminder.internal.common.android.database

import com.nlab.reminder.domain.common.schedule.Schedule
import com.nlab.reminder.internal.data.model.toModels

/**
 * @author Doohyun
 */
fun ScheduleEntityWithTagEntities.toSchedule(): Schedule =
    scheduleEntity.toSchedule(tags = tagEntities.toModels().sortedBy { it.name })