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

package com.nlab.reminder.core.local.datastore.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.nlab.reminder.core.kotlin.Result
import kotlinx.coroutines.flow.Flow

/**
 * @author Doohyun
 */
class PreferenceDataSource internal constructor(private val dataStore: DataStore<Preferences>) {
    fun getAllScheduleCompleteShownAsStream(): Flow<Boolean> = dataStore.getAsStream(
        key = PreferenceKeys.ALL_SCHEDULE_COMPLETE_SHOWN,
        defaultValue = false
    )

    suspend fun setAllScheduleCompleteShown(value: Boolean): Result<Unit> = dataStore.tryEdit(
        key = PreferenceKeys.ALL_SCHEDULE_COMPLETE_SHOWN,
        value
    )
}