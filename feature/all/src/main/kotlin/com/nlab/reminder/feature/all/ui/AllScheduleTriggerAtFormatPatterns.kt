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

package com.nlab.reminder.feature.all.ui

import android.content.res.Resources
import com.nlab.reminder.core.component.displayformat.ui.DateTimeFormatPattern
import com.nlab.reminder.core.component.displayformat.ui.triggerAtDateTimeFormatPatternForList
import com.nlab.reminder.core.component.schedulelist.content.ui.TriggerAtFormatPatterns
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * @author Doohyun
 */
class AllScheduleTriggerAtFormatPatterns : TriggerAtFormatPatterns {
    override fun get(resources: Resources, triggerAt: LocalDateTime, entryAt: LocalDateTime): DateTimeFormatPattern {
        return triggerAtDateTimeFormatPatternForList(resources, triggerAt, entryAt)
    }

    override fun get(resources: Resources, triggerAt: LocalDate, entryAt: LocalDateTime): DateTimeFormatPattern {
        return triggerAtDateTimeFormatPatternForList(resources, triggerAt, entryAt.date)
    }
}