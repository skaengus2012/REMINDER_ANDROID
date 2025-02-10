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
        REPEAT_FREQUENCY_SETTING_WEEKLY,
        REPEAT_FREQUENCY_SETTING_MONTHLY_DAY,
        REPEAT_FREQUENCY_SETTING_MONTHLY_DAY_ORDER,
        REPEAT_FREQUENCY_SETTING_MONTHLY_DAY_OF_WEEK,
        REPEAT_FREQUENCY_SETTING_YEARLY_MONTH,
        REPEAT_FREQUENCY_SETTING_YEARLY_DAY_ORDER,
        REPEAT_FREQUENCY_SETTING_YEARLY_DAY_OF_WEEK
    ]
)
@Retention(AnnotationRetention.SOURCE)
annotation class RepeatFrequencySettingCode

const val REPEAT_FREQUENCY_SETTING_WEEKLY = "REPEAT_FREQUENCY_SETTING_WEEKLY"
const val REPEAT_FREQUENCY_SETTING_MONTHLY_DAY = "REPEAT_FREQUENCY_SETTING_MONTHLY_DAY"
const val REPEAT_FREQUENCY_SETTING_MONTHLY_DAY_ORDER = "REPEAT_FREQUENCY_SETTING_MONTHLY_DAY_ORDER"
const val REPEAT_FREQUENCY_SETTING_MONTHLY_DAY_OF_WEEK = "REPEAT_FREQUENCY_SETTING_MONTHLY_DAY_OF_WEEK"
const val REPEAT_FREQUENCY_SETTING_YEARLY_MONTH = "REPEAT_FREQUENCY_SETTING_YEARLY_MONTH"
const val REPEAT_FREQUENCY_SETTING_YEARLY_DAY_ORDER = "REPEAT_FREQUENCY_SETTING_YEARLY_DAY_ORDER"
const val REPEAT_FREQUENCY_SETTING_YEARLY_DAY_OF_WEEK = "REPEAT_FREQUENCY_SETTING_YEARLY_DAY_OF_WEEK"