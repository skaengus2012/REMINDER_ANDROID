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

package com.nlab.reminder.core.statekit.plugins

import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.trueValue
import org.junit.Test

/**
 * @author Thalys
 */
class StoreConfigurationTest {
    @Test
    fun `Default StoreConfiguration has empty value`() {
        val storeConfiguration = StoreConfiguration()
        assertThat(storeConfiguration.preferredCoroutineDispatcher, nullValue())
        assertThat(storeConfiguration.defaultCoroutineExceptionHandler, nullValue())
        assertThat(storeConfiguration.defaultEffects.isEmpty(), trueValue())
        assertThat(storeConfiguration.defaultSuspendEffects.isEmpty(), trueValue())
    }
}