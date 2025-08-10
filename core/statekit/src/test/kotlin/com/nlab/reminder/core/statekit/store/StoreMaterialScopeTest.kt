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

package com.nlab.reminder.core.statekit.store

import com.nlab.reminder.core.statekit.plugins.StateKitAndroidPlugin
import com.nlab.reminder.core.statekit.plugins.StoreConfiguration
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class StoreMaterialScopeTest {
    @Test
    fun `Given coroutineScope, custom global configuration, When create, Then object has correct properties`() {
        val coroutineScope: CoroutineScope = mockk()
        val defaultConfiguration = StateKitAndroidPlugin.globalStoreConfiguration
        val customConfiguration = StoreConfiguration(
            preferredCoroutineDispatcher = Dispatchers.IO
        )
        StateKitAndroidPlugin.setGlobalStoreConfiguration(customConfiguration)

        val storeMaterialScope = StoreMaterialScope(baseCoroutineScope = coroutineScope)
        assertThat(storeMaterialScope.configuration, sameInstance(customConfiguration))
        assertThat(storeMaterialScope.baseCoroutineScope, sameInstance(coroutineScope))

        StateKitAndroidPlugin.setGlobalStoreConfiguration(defaultConfiguration)
    }

    @Test
    fun `Given custom configuration, When create with, Then object has correct configuration`() {
        val customConfiguration = StoreConfiguration(preferredCoroutineDispatcher = Dispatchers.IO)
        val storeMaterialScope = StoreMaterialScope(baseCoroutineScope = mockk())

        val newStoreMaterialScope = storeMaterialScope.with(customConfiguration)
        assertThat(newStoreMaterialScope.configuration, sameInstance(customConfiguration))
    }
}