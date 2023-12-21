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

package com.nlab.reminder.internal.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.nlab.reminder.core.kotlin.util.isFailure
import com.nlab.reminder.core.kotlin.util.isSuccess
import com.nlab.testkit.genBoolean
import com.nlab.testkit.genBothify
import com.nlab.testkit.once
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.mockito.kotlin.UseConstructor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author thalys
 */
internal class LocalCompletedScheduleShownRepositoryTest {
    @Test
    fun `Get from preference`() = runTest {
        val expectedShown = genBoolean()
        val preferenceKey = genBooleanPreferencesKey()

        testGetAsStream(
            preferenceKey,
            expectedShown,
            preferences = mock(useConstructor = UseConstructor.parameterless()) {
                whenever(mock[preferenceKey]) doReturn expectedShown
            }
        )
    }

    @Test
    fun `Get false, when preference was empty`() = runTest {
        val preferenceKey = genBooleanPreferencesKey()

        testGetAsStream(
            preferenceKey,
            expectedShown = false,
            preferences = mock(useConstructor = UseConstructor.parameterless()) {
                whenever(mock[preferenceKey]) doReturn null
            }
        )
    }

    @Test
    fun `Preferences save shown when repository sent isShown`() = runTest {
        val expectedShown = genBoolean()
        val preferenceKey = genBooleanPreferencesKey()
        val mutablePreferences: MutablePreferences = mock(useConstructor = UseConstructor.parameterless())
        val preferences: Preferences = mock(useConstructor = UseConstructor.parameterless()) {
            whenever(mock.toMutablePreferences()) doReturn mutablePreferences
        }
        val fakeDataStore: DataStore<Preferences> = genFakeDataSource { transform -> transform(preferences) }

        LocalCompletedScheduleShownRepository(fakeDataStore, preferenceKey)
            .setShown(expectedShown)
            .isSuccess
            .run(::assert)
        verify(mutablePreferences, once())[preferenceKey] = expectedShown
    }

    @Test
    fun `occurred error when repository sent isShown`() = runTest {
        val expectedShown = genBoolean()
        val preferenceKey = genBooleanPreferencesKey()
        val fakeDataStore: DataStore<Preferences> = genFakeDataSource { throw Throwable() }

        LocalCompletedScheduleShownRepository(fakeDataStore, preferenceKey)
            .setShown(expectedShown)
            .isFailure
            .run(::assert)
    }
}

private fun genBooleanPreferencesKey(): Preferences.Key<Boolean> =
    booleanPreferencesKey(genBothify())

private suspend fun testGetAsStream(
    preferenceKey: Preferences.Key<Boolean>,
    expectedShown: Boolean,
    preferences: Preferences
) {
    val dataStore: DataStore<Preferences> = mock {
        whenever(mock.data) doReturn flowOf(preferences)
    }
    val actualShown: Boolean =
        LocalCompletedScheduleShownRepository(dataStore, preferenceKey)
            .getAsStream()
            .first()
    MatcherAssert.assertThat(actualShown, CoreMatchers.equalTo(expectedShown))
}

private fun genFakeDataSource(
    updateDataDelegate: suspend (transform: suspend (Preferences) -> Preferences) -> Preferences
): DataStore<Preferences> = object : DataStore<Preferences> {
    override val data: Flow<Preferences> = emptyFlow()
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        return updateDataDelegate(transform)
    }
}