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

package com.nlab.reminder.core.component.schedule.ui.view.list

import com.nlab.reminder.core.data.model.ScheduleTiming
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

/**
 * @author Doohyun
 */
internal class ScheduleTimingDisplayTextPool {
    private val cache = hashMapOf<ScheduleTiming, ScheduleTimingDisplayTextCachedValue>()

    internal inline fun getOrPut(
        scheduleTiming: ScheduleTiming,
        scheduleCompleted: Boolean,
        timeZone: TimeZone,
        entryAt: Instant,
        provideNewDisplayText: () -> CharSequence
    ): CharSequence {
        val cachedValue = cache[scheduleTiming]
        if (cachedValue == null) {
            val newValue = ScheduleTimingDisplayTextCachedValue(
                scheduleCompleted,
                timeZone,
                entryAt,
                provideNewDisplayText()
            )
            cache[scheduleTiming] = newValue
            return newValue.displayText
        }

        if (cachedValue.scheduleCompleted != scheduleCompleted
            || cachedValue.timeZone != timeZone
            || cachedValue.entryAt != entryAt) {
            val newDisplayText = provideNewDisplayText()
            cachedValue.updateValue(
                scheduleCompleted,
                timeZone,
                entryAt,
                newDisplayText
            )
            return newDisplayText
        }

        return cachedValue.displayText
    }
}

internal class ScheduleTimingDisplayTextCachedValue(
    scheduleCompleted: Boolean,
    timeZone: TimeZone,
    entryAt: Instant,
    displayText: CharSequence
) {
    var scheduleCompleted: Boolean = scheduleCompleted
        private set
    var timeZone: TimeZone = timeZone
        private set
    var entryAt: Instant = entryAt
        private set
    var displayText: CharSequence = displayText
        private set

    fun updateValue(scheduleCompleted: Boolean, timeZone: TimeZone, entryAt: Instant, displayText: CharSequence) {
        this.scheduleCompleted = scheduleCompleted
        this.timeZone = timeZone
        this.entryAt = entryAt
        this.displayText = displayText
    }
}