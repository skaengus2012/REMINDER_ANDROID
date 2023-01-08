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

package com.nlab.reminder.internal.common.schedule.visibleconfig.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.nlab.reminder.test.genBoolean
import com.nlab.reminder.test.genBothify
import com.nlab.reminder.test.once
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.*
import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.core.kotlin.util.isFailure
import com.nlab.reminder.core.kotlin.util.isSuccess
import com.nlab.reminder.internal.common.schedule.visibleconfig.impl.LocalCompletedScheduleShownRepository

/**
 * @author thalys
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocalCompletedScheduleShownRepositoryTest {
    @Test
    fun `notify is complete shown`() = runTest {
        val expectedShown = genBoolean()
        val preferenceKey = booleanPreferencesKey(genBothify())
        testGet(
            preferenceKey,
            expectedShown,
            preferences = mock(useConstructor = UseConstructor.parameterless()) {
                whenever(mock[preferenceKey]) doReturn expectedShown
            }
        )
    }

    @Test
    fun `notify false when preference was empty`() = runTest {
        val preferenceKey = booleanPreferencesKey(genBothify())
        testGet(
            preferenceKey,
            expectedShown = false,
            preferences = mock(useConstructor = UseConstructor.parameterless()) {
                whenever(mock[preferenceKey]) doReturn null
            }
        )
    }

    private suspend fun testGet(
        preferenceKey: Preferences.Key<Boolean>,
        expectedShown: Boolean,
        preferences: Preferences
    ) {
        val dataStore: DataStore<Preferences> = mock {
            whenever(mock.data) doReturn flowOf(preferences)
        }
        val actualShown: Boolean =
            LocalCompletedScheduleShownRepository(dataStore, preferenceKey)
                .get()
                .first()
        assertThat(actualShown, equalTo(expectedShown))
    }

    @Test
    fun `preferences save shown when repository sent isShown`() = runTest {
        val isShown = genBoolean()
        val expectedShown = isShown.not()
        val preferenceKey = booleanPreferencesKey(genBothify())
        val mutablePreferences: MutablePreferences = mock(useConstructor = UseConstructor.parameterless())
        val preferences: Preferences = mock(useConstructor = UseConstructor.parameterless()) {
            whenever(mock.toMutablePreferences()) doReturn mutablePreferences
        }
        val fakeDataStore: DataStore<Preferences> = object : DataStore<Preferences> {
            override val data: Flow<Preferences> = emptyFlow()
            override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
                return transform(preferences)
            }
        }

        val result: Result<Unit> =
            LocalCompletedScheduleShownRepository(fakeDataStore, preferenceKey)
                .setShown(expectedShown)
        assertThat(
            result.isSuccess,
            equalTo(true)
        )
        verify(mutablePreferences, once())[preferenceKey] = expectedShown
    }

    @Test
    fun `occurred error when repository sent isShown`() = runTest {
        val isShown = genBoolean()
        val expectedShown = isShown.not()
        val preferenceKey = booleanPreferencesKey(genBothify())
        val fakeDataStore: DataStore<Preferences> = object : DataStore<Preferences> {
            override val data: Flow<Preferences> = emptyFlow()
            override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
                throw Throwable()
            }
        }

        val result: Result<Unit> =
            LocalCompletedScheduleShownRepository(fakeDataStore, preferenceKey)
                .setShown(expectedShown)
        assertThat(
            result.isFailure,
            equalTo(true)
        )
    }
}