/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.internal.common.android.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.core.kotlin.util.catching
import com.nlab.reminder.core.util.test.annotation.ExcludeFromGeneratedTestReport

/**
 * @author thalys
 */
// jacoco cannot recognize catching with edit
@ExcludeFromGeneratedTestReport
class EditDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    suspend operator fun invoke(transform: (MutablePreferences) -> Unit): Result<Unit> = catching {
        dataStore.edit(transform)
    }
}