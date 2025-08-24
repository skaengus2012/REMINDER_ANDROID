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

package com.nlab.statekit.foundation.plugins

import io.mockk.mockk
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class StateKitPluginTest {
    @Test
    fun `When get global store configuration, Then return empty configuration`() {
        val storeConfiguration = StateKitPlugin.globalStoreConfiguration
        assertThat(storeConfiguration, equalTo(StoreConfiguration()))
    }

    @Test
    fun `Given custom configuration, When config global store, Then return custom configuration`() {
        // before
        val defaultConfiguration = StateKitPlugin.globalStoreConfiguration

        // test
        val configuration = StoreConfiguration(
            preferredCoroutineDispatcher = mockk(),
            defaultCoroutineExceptionHandler = mockk(),
            defaultEffects = mockk(),
            defaultSuspendEffects = mockk()
        )
        StateKitPlugin.configGlobalStore { configuration }

        val actualConfiguration = StateKitPlugin.globalStoreConfiguration
        assertThat(actualConfiguration, sameInstance(configuration))

        // after
        StateKitPlugin.configGlobalStore { defaultConfiguration }
    }
}