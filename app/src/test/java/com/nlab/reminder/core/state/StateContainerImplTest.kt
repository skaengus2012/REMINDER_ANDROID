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
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class StateContainerImplTest {
    @Test
    fun `eventProcessor sent event when state container sent`() = runTest {
        val eventProcessor: EventProcessor<TestEvent> = mock()
        val stateMachine = StateContainerImpl<TestEvent, TestState>(eventProcessor, mock())
        val event = TestEvent.Event1()
        stateMachine.send(event)
        verify(eventProcessor, times(1)).send(event)
    }
}