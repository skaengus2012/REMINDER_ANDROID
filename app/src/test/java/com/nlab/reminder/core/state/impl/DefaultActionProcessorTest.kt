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

package com.nlab.reminder.core.state.impl

import com.nlab.reminder.core.state.ActionProcessor
import com.nlab.reminder.core.state.TestAction
import com.nlab.reminder.core.state.TestState
import kotlinx.coroutines.*
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultActionProcessorTest {
    @Test
    fun `Receive action when action is sent to actionProcessor`() = runTest {
        val actionReceiver: (TestAction) -> Unit = mock()
        val actionProcessor = DefaultActionProcessor(
            CoroutineScope(Dispatchers.Default),
            actionReceiver
        )
        val action = TestAction.Action1()

        actionProcessor
            .send(action)
            .join()
        verify(actionReceiver, times(1))(action)
    }

    @Test
    fun `execution completes asynchronously when event is sent 1000 times`() = runTest {
        val onReceiveState: (TestState) -> Unit = mock()
        val stateReducer: ActionProcessor<TestAction> = DefaultActionProcessor(
            scope = this,
            onActionReceived = { delay(timeMillis = 5000); TestState.State1().also { onReceiveState(it); } },
        )

        (1..1000)
            .map { stateReducer.send(TestAction.Action1()) }
            .joinAll()
        advanceTimeBy(delayTimeMillis = 5500)
        verify(onReceiveState, times(1000)).invoke(any())
    }
}