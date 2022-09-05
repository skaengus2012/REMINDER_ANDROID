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

package com.nlab.reminder.core.effect.util

import com.nlab.reminder.core.effect.SendSideEffect
import com.nlab.reminder.core.effect.DepreTestSideEffectMessage
import com.nlab.reminder.test.genInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Test

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SideEffectsKtTest {
    @Test
    fun `notify message when sideEffect invoked`() = runTest {
        Dispatchers.setMain(Dispatchers.Unconfined)
        val testSideEffect: SendSideEffect<DepreTestSideEffectMessage> by sideEffect()
        val tryCount = genInt("##").coerceAtMost(30)
        (1..tryCount)
            .map { DepreTestSideEffectMessage(it) }
            .forEach { message -> testSideEffect.send(message) }
        assertThat(
            testSideEffect.event
                .take(tryCount)
                .fold(0) { acc, value -> acc + value.number },
            equalTo((1..tryCount).reduce { acc, i -> acc + i })
        )
    }
}