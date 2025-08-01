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

package com.nlab.reminder.core.component.displayformat.ui

import androidx.annotation.StringRes
import com.nlab.reminder.core.data.model.DaysOfWeekOrder
import com.nlab.reminder.core.translation.StringIds

/**
 * @author Thalys
 */
@get:StringRes
internal val DaysOfWeekOrder.resourceId: Int
    get() = when (this) {
        DaysOfWeekOrder.First -> StringIds.first
        DaysOfWeekOrder.Second -> StringIds.second
        DaysOfWeekOrder.Third -> StringIds.third
        DaysOfWeekOrder.Fourth -> StringIds.fourth
        DaysOfWeekOrder.Fifth -> StringIds.fifth
        DaysOfWeekOrder.Last -> StringIds.last
    }