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

package com.nlab.reminder.core.effect

import com.nlab.testkit.genBoolean
import com.nlab.testkit.once
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.coroutines.ContinuationInterceptor

/**
 * @author thalys
 */
@OptIn(
    ExperimentalCoroutinesApi::class,
    DelicateCoroutinesApi::class
)
class SideEffectContainerTest {
    @Test
    fun `channel send when controller sent sideEffect`() = runTest {
        val message = TestSideEffect(genBoolean())
        val channel: Channel<TestSideEffect> = mock()

        SideEffectContainer(channel, Dispatchers.Default).post(message)
        verify(channel, once()).send(message)
    }

    @Test
    fun `sent sideEffect on single dispatcher`() = runTest {
        val testDispatcher = newSingleThreadContext("SingleDispatcher")
        val message = TestSideEffect(genBoolean())
        val channel: Channel<TestSideEffect> = Channel(Channel.UNLIMITED)
        val isContextEquals = CompletableDeferred<Boolean>()
        val fakeChannel: Channel<TestSideEffect> = object : Channel<TestSideEffect> by channel {
            override suspend fun send(element: TestSideEffect) {
                val curContext = coroutineScope {
                    coroutineContext[ContinuationInterceptor]
                }
                val expectedContext = withContext(testDispatcher) {
                    coroutineContext[ContinuationInterceptor]
                }
                isContextEquals.complete(curContext === expectedContext)
                channel.send(element)
            }
        }

        SideEffectContainer(fakeChannel, testDispatcher).post(message)
        assertThat(isContextEquals.await(), equalTo(true))
    }

    @Test
    fun `notify 100 times message after sending message 100 times`() = runTest {
        val testCount = 100
        val controller = SideEffectContainer<TestSideEffect>(Channel(Channel.UNLIMITED), Dispatchers.Default)
        (1..testCount)
            .map { number -> launch { controller.post(TestSideEffect(number)) } }
            .joinAll()

        assertThat(
            withContext(Dispatchers.Default) {
                controller.sideEffectFlow
                    .take(testCount)
                    .fold(0) { acc, message -> acc + message.value as Int }
            },
            equalTo((1..testCount).reduce { acc, i -> acc + i })
        )
    }
}