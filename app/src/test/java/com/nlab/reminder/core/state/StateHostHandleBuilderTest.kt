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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author thalys
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StateHostHandleBuilderTest {
    @Test
    fun `handled when current was any state`() = runTest {
        testOnceHandleTemplate { action ->
            handled {
                anyEvent {
                    anyState { action() }
                }
            }
        }
    }

    @Test
    fun `handled when current was specific state`() = runTest {
        val fixedInitState = TestState.genState()
        testOnceHandleTemplate(initState = fixedInitState) { action ->
            handled {
                anyEvent {
                    filteredState(predicate = { state -> state === fixedInitState }) { action() }
                }
            }
        }
    }

    @Test
    fun `never handled when current was specific state`() = runTest {
        val fixedInitState = TestState.genState()
        testNeverHandleTemplate(initState = fixedInitState) { action ->
            handled {
                anyEvent {
                    filteredState(predicate = { state -> state !== fixedInitState }) { action() }
                }
            }
        }
    }

    @Test
    fun `handled when current was init`() = runTest {
        testOnceHandleTemplate(initState = TestState.StateInit()) { action ->
            handled {
                anyEvent {
                    state<TestState.StateInit> { action() }
                }
            }
        }
    }

    @Test
    fun `handled when current was not init`() = runTest {
        testOnceHandleTemplate(initState = TestState.State1()) { action ->
            handled {
                anyEvent {
                    stateNot<TestState.StateInit> { action() }
                }
            }
        }
    }

    @Test
    fun `never handled when current was init`() = runTest {
        testNeverHandleTemplate(initState = TestState.StateInit()) { action ->
            handled {
                anyEvent {
                    stateNot<TestState.StateInit> { action() }
                }
            }
        }
    }
}