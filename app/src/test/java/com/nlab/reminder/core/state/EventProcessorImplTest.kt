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
internal class EventProcessorImplTest {
    @Test
    fun `Receive event when action is sent to actionProcessor`() = runTest {
        val eventReceiver: (TestEvent) -> Unit = mock()
        val eventProcessor = EventProcessorImpl(
            CoroutineScope(Dispatchers.Default),
            eventReceiver
        )
        val action = TestEvent.Event1()

        eventProcessor
            .send(action)
            .join()
        verify(eventReceiver, times(1))(action)
    }

    @Test
    fun `execution completes asynchronously when event is sent 1000 times`() = runTest {
        val onReceiveState: (TestState) -> Unit = mock()
        val stateReducer: EventProcessor<TestEvent> = EventProcessorImpl(
            scope = this,
            onEventReceived = { delay(timeMillis = 5000); TestState.State1().also { onReceiveState(it); } },
        )

        (1..1000)
            .map { stateReducer.send(TestEvent.Event1()) }
            .joinAll()
        advanceTimeBy(delayTimeMillis = 5500)
        verify(onReceiveState, times(1000)).invoke(any())
    }
}