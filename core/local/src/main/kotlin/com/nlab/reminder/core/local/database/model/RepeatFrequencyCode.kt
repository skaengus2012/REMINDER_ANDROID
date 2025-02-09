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
        REPEAT_FREQUENCY_DAILY,
        REPEAT_FREQUENCY_WEEKLY,
        REPEAT_FREQUENCY_MONTHLY,
        REPEAT_FREQUENCY_YEARLY
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class RepeatFrequencyCode

const val REPEAT_FREQUENCY_DAILY = "REPEAT_FREQUENCY_DAILY"
const val REPEAT_FREQUENCY_WEEKLY = "REPEAT_FREQUENCY_WEEKLY"
const val REPEAT_FREQUENCY_MONTHLY = "REPEAT_FREQUENCY_MONTHLY"
const val REPEAT_FREQUENCY_YEARLY = "REPEAT_FREQUENCY_YEARLY"