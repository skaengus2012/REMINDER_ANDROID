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

import com.nlab.reminder.test.instanceOf
import com.nlab.reminder.test.once
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StateMachineKtTest {
    @Test
    fun `update to state1 when current was init and event1 sent`() = runTest {
        val stateMachine = StateMachine<TestEvent, TestState> {
            reduce {
                state<TestState.StateInit> {
                    event<TestEvent.Event1> {
                        TestState.State1()
                    }
                }
            }
        }
        val stateController = stateMachine.asContainer(CoroutineScope(Dispatchers.Default), TestState.StateInit())
        stateController
            .send(TestEvent.Event1())
            .join()
        assertThat(
            stateController.stateFlow.value,
            instanceOf(TestState.State1::class)
        )
    }

    @Test
    fun `sent event1 by fetching when container started publish`() = runTest {
        val action: () -> Unit = mock()
        val stateMachine = StateMachine<TestEvent, TestState> {
            handle {
                anyState {
                    event<TestEvent.Event1> { action() }
                }
            }
        }
        val controller: StateContainer<TestEvent, TestState> =
            stateMachine
                .asContainer(
                    CoroutineScope(Dispatchers.Unconfined),
                    TestState.genState(),
                    fetchEvent = TestEvent.Event1()
                )
        verify(action, never())()

        controller.stateFlow.take(1).collect()
        verify(action, once())()
    }
}