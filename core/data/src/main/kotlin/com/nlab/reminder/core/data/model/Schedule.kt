/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.data.model

import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.NonNegativeLong

/**
 * @author Doohyun
 */
data class Schedule(
    val id: ScheduleId,
    val title: NonBlankString,
    val note: NonBlankString?,
    val link: Link?,
    val triggerTime: TriggerTime?,
    val repeat: Repeat?,
    val visiblePriority: NonNegativeLong,
    val isComplete: Boolean
)