/*
 * Copyright (C) 2024 The N's lab Open Source Project
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
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.catching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @author Doohyun
 */
internal suspend fun DataStore<Preferences>.tryEdit(
    transform: suspend (MutablePreferences) -> Unit
): Result<Unit> = catching { edit(transform) }

internal suspend fun <T> DataStore<Preferences>.tryEdit(
    key: Preferences.Key<T>,
    value: T
): Result<Unit> = tryEdit { preferences -> preferences[key] = value }

internal fun <T> DataStore<Preferences>.getAsStream(
    key: Preferences.Key<T>,
    defaultValue: T
): Flow<T> = data.map { it[key] ?: defaultValue }