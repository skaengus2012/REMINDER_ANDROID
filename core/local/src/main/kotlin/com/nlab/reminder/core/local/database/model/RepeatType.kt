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

package com.nlab.reminder.core.local.database.model

import androidx.annotation.StringDef

/**
 * @author Thalys
 */
@StringDef(
    value = [
        REPEAT_HOURLY,
        REPEAT_DAILY,
        REPEAT_WEEKLY,
        REPEAT_MONTHLY,
        REPEAT_YEARLY
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class RepeatType

const val REPEAT_HOURLY = "REPEAT_HOURLY"
const val REPEAT_DAILY = "REPEAT_DAILY"
const val REPEAT_WEEKLY = "REPEAT_WEEKLY"
const val REPEAT_MONTHLY = "REPEAT_MONTHLY"
const val REPEAT_YEARLY = "REPEAT_YEARLY"