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

package com.nlab.statekit

import com.nlab.testkit.faker.genInt

/**
 * @author thalys
 */
sealed class TestState private constructor() {
    data object State1 : TestState()
    data object State2 : TestState()
    data object State3 : TestState()

    companion object {
        fun genState(): TestState = when (genInt() % 3) {
            0 -> State1
            1 -> State2
            else -> State3
        }
    }
}