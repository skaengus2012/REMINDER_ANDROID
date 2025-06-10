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

package com.nlab.reminder.core.data.model.ui

import androidx.annotation.StringRes
import com.nlab.reminder.core.data.model.Days
import com.nlab.reminder.core.translation.StringIds

/**
 * @author Doohyun
 */
@get:StringRes
internal val Days.resourceId: Int
    get() = when (this) {
        Days.Sun -> StringIds.sunday
        Days.Mon -> StringIds.monday
        Days.Tue -> StringIds.tuesday
        Days.Wed -> StringIds.wednesday
        Days.Thu -> StringIds.thursday
        Days.Fri -> StringIds.friday
        Days.Sat -> StringIds.saturday
        Days.Default -> StringIds.day
        Days.Weekday -> StringIds.weekday
        Days.Weekend -> StringIds.weekend_day
    }