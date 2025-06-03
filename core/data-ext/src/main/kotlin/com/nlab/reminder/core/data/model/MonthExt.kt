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

package com.nlab.reminder.core.data.model

import androidx.annotation.StringRes
import com.nlab.reminder.core.translation.StringIds
import kotlinx.datetime.Month

/**
 * @author Doohyun
 */
@get:StringRes
internal val Month.fullNameResourceId: Int
    get() = when (this) {
        Month.JANUARY -> StringIds.january
        Month.FEBRUARY -> StringIds.february
        Month.MARCH -> StringIds.march
        Month.APRIL -> StringIds.april
        Month.MAY -> StringIds.may
        Month.JUNE -> StringIds.june
        Month.JULY -> StringIds.july
        Month.AUGUST -> StringIds.august
        Month.SEPTEMBER -> StringIds.september
        Month.OCTOBER -> StringIds.october
        Month.NOVEMBER -> StringIds.november
        Month.DECEMBER -> StringIds.december
    }

@get:StringRes
internal val Month.shortNameResourceId: Int
    get() = when (this) {
        Month.JANUARY -> StringIds.january_short
        Month.FEBRUARY -> StringIds.february_short
        Month.MARCH -> StringIds.march_short
        Month.APRIL -> StringIds.april_short
        Month.MAY -> StringIds.may_short
        Month.JUNE -> StringIds.june_short
        Month.JULY -> StringIds.july_short
        Month.AUGUST -> StringIds.august_short
        Month.SEPTEMBER -> StringIds.september_short
        Month.OCTOBER -> StringIds.october_short
        Month.NOVEMBER -> StringIds.november_short
        Month.DECEMBER -> StringIds.december_short
    }