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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StateMachineHandleScopeTest {
    @Test
    fun `flow restart when state subscription was recreated`() = runTest {
        val startNumber = genInt()
        val sequenceFlow = flow {
            var number = startNumber
            while (true) emit(number++)
        }
        val actionFlow: MutableSharedFlow<Int> = MutableSharedFlow()
        val state = MutableStateFlow<TestState>(TestState.StateInit())
        val eventProcessor = genTestStateMachineEventProcessor(state = state, stateMachine = StateMachine {
            handled {
                event<TestEvent.Event1> {
                    state<TestState.StateInit> {
                        // jacoco make branch if middle operator not existed
                        sequenceFlow.take(10).collectWithMachine(actionFlow::emit)
                    }
                }
            }
        })
        suspend fun getActionEmitResult(): Int {
            val job = launch { state.collect() }
            val emitResult = actionFlow.take(1).first()
            job.cancelAndJoin()
            state.subscriptionCount.filter { it == 0 }.take(1).collect()

            return emitResult
        }

        eventProcessor.send(TestEvent.Event1())
        assertThat(getActionEmitResult() + getActionEmitResult(), equalTo(startNumber * 2))
    }
}