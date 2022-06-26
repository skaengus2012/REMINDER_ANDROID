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

package com.nlab.practice2021.core.effect.impl

import com.nlab.practice2021.core.effect.SendSideEffect
import com.nlab.practice2021.core.effect.TestSideEffectMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSideEffectTest {
    @Before
    fun init() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @Test
    fun `notify 64 times event after sending message 64 times`() {
        joinSendJobsAndReceiveTest(
            // DefaultSideEffect use Buffered channel.
            // default buffer size is 64
            DefaultSideEffect(Channel(Channel.BUFFERED)),
            testCount = 64
        )
    }

    @Test
    fun `notify 100 times event after sending message 100 times with unlimited channel`() {
        joinSendJobsAndReceiveTest(
            DefaultSideEffect(Channel(Channel.UNLIMITED)),
            testCount = 100
        )
    }

    private fun joinSendJobsAndReceiveTest(
        sendSideEffect: SendSideEffect<TestSideEffectMessage>,
        testCount: Int
    ) = runTest {
        (1..testCount)
            .map { number -> launch { sendSideEffect.send(TestSideEffectMessage(number)) } }
            .joinAll()

        assertThat(
            withContext(Dispatchers.Default) {
                sendSideEffect.event
                    .take(testCount)
                    .fold(0) { acc, value -> acc + value.number }
            },
            equalTo((1..testCount).reduce { acc, i -> acc + i })
        )
    }
}