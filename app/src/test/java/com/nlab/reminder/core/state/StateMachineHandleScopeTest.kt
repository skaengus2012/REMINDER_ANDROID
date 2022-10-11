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

import com.nlab.reminder.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

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
        val sequenceRouteFlow: MutableSharedFlow<Int> = MutableSharedFlow()
        val state = MutableStateFlow(Unit)
        val scope = StateMachineHandleScope<TestEvent>(state.subscriptionCount, mock())
        val subscribeJob: Job = launch(Dispatchers.Default) {
            scope.run {
                sequenceFlow.collectWhileSubscribed(sequenceRouteFlow::emit)
            }
        }

        suspend fun getRouteResult(): Int {
            val job = launch { state.collect() }
            val emitResult = sequenceRouteFlow.take(1).first()
            job.cancelAndJoin()
            state.subscriptionCount.filter { it == 0 }.take(1).collect()

            return emitResult
        }

        val totalResult = getRouteResult() + getRouteResult()
        subscribeJob.cancelAndJoin()

        assertThat(totalResult, equalTo(startNumber * 2))
    }
}