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

import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.reminder.core.kotlin.toNonNegativeLong
import com.nlab.reminder.core.kotlin.tryToNonBlankStringOrNull
import com.nlab.reminder.core.local.database.model.ScheduleWithDetailsEntity

/**
 * @author Doohyun
 */
internal fun Schedule(entity: ScheduleWithDetailsEntity): Schedule = Schedule(
    id = ScheduleId(entity.schedule.scheduleId),
    content = ScheduleContent(entity),
    visiblePriority = entity.schedule.visiblePriority.toNonNegativeLong(),
    isComplete = entity.schedule.isComplete
)

internal fun ScheduleContent(entity: ScheduleWithDetailsEntity): ScheduleContent = ScheduleContent(
    title = entity.schedule.title.toNonBlankString(),
    note = entity.schedule.description.tryToNonBlankStringOrNull(),
    link = entity.schedule.link.tryToNonBlankStringOrNull()?.let(::Link),
    triggerTime = with(entity.schedule) {
        val curTimeUtc = triggerTimeUtc
        val curTriggerTimeDateOnly = isTriggerTimeDateOnly
        when {
            curTimeUtc == null && curTriggerTimeDateOnly == null -> null
            curTimeUtc != null && curTriggerTimeDateOnly != null -> {
                TriggerTime(
                    utcTime = curTimeUtc,
                    isDateOnly = curTriggerTimeDateOnly
                )
            }

            else -> throw IllegalArgumentException("Invalid TriggerTime [$curTimeUtc, $curTriggerTimeDateOnly]")
        }
    },
    repeat = with(entity) {
        val curRepeatType = entity.schedule.repeatType
        val curRepeatInterval = entity.schedule.repeatInterval
        /**
         * If RepeatDetails is incorrectly entered, it is not confirmed.
         * In Repeat function, check if there are appropriate repeatDetails.
         *
         * The validity of the repeatDetails should be checked only in the data insertion.
         * @see [com.nlab.reminder.core.local.database.transaction.ScheduleTransactionValidator]
         */
        when {
            curRepeatType == null && curRepeatInterval == null -> null
            curRepeatType != null && curRepeatInterval != null -> {
                Repeat(
                    type = curRepeatType,
                    interval = curRepeatInterval,
                    detailEntities = entity.repeatDetails
                )
            }

            else -> throw IllegalArgumentException("Invalid RepeatDetails [$curRepeatType, $curRepeatInterval]")
        }
    }
)