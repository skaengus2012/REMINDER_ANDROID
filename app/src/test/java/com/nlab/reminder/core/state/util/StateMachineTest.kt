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

package com.nlab.reminder.core.state.util

import com.nlab.reminder.core.state.StateController
import com.nlab.reminder.core.state.TestEvent
import com.nlab.reminder.core.state.TestState
import com.nlab.reminder.test.genFlowObserveDispatcher
import com.nlab.reminder.test.instanceOf
import com.nlab.reminder.test.once
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.*
import org.junit.Test
import org.hamcrest.MatcherAssert.assertThat
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StateMachineTest {
    @Test
    fun `update to state1 when state was init and testAction1 sent`() = runTest {
        val stateMachine = StateMachine<TestEvent, TestState> {
            update { (action, old) ->
                when (action) {
                    is TestEvent.Event1 -> {
                        if (old is TestState.StateInit) TestState.State1()
                        else TestState.State2()
                    }
                    is TestEvent.Event2 -> TestState.State2()
                }
            }
        }
        val stateController = stateMachine.controlIn(CoroutineScope(Dispatchers.Default), TestState.StateInit())
        stateController
            .send(TestEvent.Event1())
            .join()
        assertThat(
            stateController.state.value,
            instanceOf(TestState.State1::class)
        )
    }

    @Test
    fun `sent testAction1 by fetching when stateController state started publish`() = runTest {
        val action: () -> Unit = mock()
        val stateMachine = StateMachine<TestEvent, TestState> {
            update { TestState.State1() }
            sideEffect { action() }
        }
        val controller: StateController<TestEvent, TestState> =
            stateMachine.controlIn(CoroutineScope(Dispatchers.Unconfined), TestState.StateInit(), TestEvent.Event1())
        verify(action, never())()
        assertThat(controller.state.value, instanceOf(TestState.StateInit::class))

        controller.state.launchIn(genFlowObserveDispatcher())
        verify(action, once())()
        assertThat(controller.state.value, instanceOf(TestState.State1::class))
    }
}