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

package com.nlab.reminder.core.statekit.bootstrap

import com.nlab.statekit.bootstrap.DeliveryStarted
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

/**
 * @author Doohyun
 */
class BootstrapFactoriesKtTest {
    @Test
    fun `When collect as Bootstrap without started, Then created with started`() {
        flowOf(1).collectAsBootstrap()
    }

    @Test
    fun `Given delivery started, When collect as Bootstrap, Then created with started`() {
        flowOf(1).collectAsBootstrap(DeliveryStarted.Lazily)
    }
}