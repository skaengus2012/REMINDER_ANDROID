/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.internal.common.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.nlab.reminder.core.kotlin.coroutine.flow.map
import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.domain.common.data.repository.CompletedScheduleShownRepository
import com.nlab.reminder.internal.common.android.datastore.EditDataStore
import kotlinx.coroutines.flow.Flow

/**
 * @author thalys
 */
internal class LocalCompletedScheduleShownRepository(
    private val dataStore: DataStore<Preferences>,
    private val preferencesKey: Preferences.Key<Boolean>
) : CompletedScheduleShownRepository {
    private val modifyShown = EditDataStore(dataStore)

    override fun getAsStream(): Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[preferencesKey] ?: false }

    override suspend fun setShown(isShown: Boolean): Result<Unit> =
        modifyShown { preference -> preference[preferencesKey] = isShown }
}