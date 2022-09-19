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

package com.nlab.reminder.core.state

import com.nlab.reminder.test.genInt

/**
 * @author Doohyun
 */
internal sealed class TestState private constructor() : State {
    class StateInit : TestState()
    class State1 : TestState()
    class State2 : TestState()

    companion object {
        fun genState(): TestState = when (genInt() % 3) {
            0 -> StateInit()
            1 -> State1()
            2 -> State2()
            else -> throw IllegalStateException("Failed to create TestState")
        }
    }
}