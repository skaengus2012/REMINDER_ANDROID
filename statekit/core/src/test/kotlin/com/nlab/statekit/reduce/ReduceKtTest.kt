/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.statekit.reduce

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import io.mockk.mockk
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


/**
 * @author Thalys
 */
class ReduceKtTest {
    @Test
    fun `When create reduce without transition, effect, Then reduce has no transition and effect`() {
        val reduce = TestReduce()
        assertThat(reduce.transition, nullValue())
        assertThat(reduce.effect, nullValue())
    }

    @Test
    fun `When create empty transition, Then reduce has no transition and effect`() {
        val reduce = EmptyReduce<TestAction, TestState>()
        assertThat(reduce.transition, nullValue())
        assertThat(reduce.effect, nullValue())
    }

    @Test
    fun `Given transition, effect, When create reduce, Then reduce has transition and effect`() {
        val transition = TestTransitionNode { _, current -> current }
        val effect = TestEffectSuspendNode { _, _, _ ->  }
        val reduce = TestReduce(transition, effect)

        assertThat(reduce.transition, sameInstance(transition))
        assertThat(reduce.effect, sameInstance(effect))
    }

    @Test
    fun `Given empty reduces, When compose reduces, Then return empty reduce`() {
        val reduces = emptyList<Reduce<TestAction, TestState>>()
        val composed = composeReduce(reduces)
        assertThat(composed.transition, nullValue())
        assertThat(composed.effect, nullValue())
    }

    @Test
    fun `Given single reduces, When compose reduces, Then return self`() {
        val reduce = EmptyReduce<TestAction, TestState>()
        val composed = composeReduce(listOf(reduce))
        assertThat(composed, sameInstance(reduce))
    }

    @Test
    fun `Given multiple transitions, When composed as reduce, Then result has all transitions`() {
        val firstTransition: TestTransition = mockk()
        val secondTransition: TestTransition = mockk()
        val thirdTransition: TestTransition = mockk()

        val reduce = composeReduce(
            TestReduce(),
            TestReduce(transition = firstTransition),
            TestReduce(transition = secondTransition),
            TestReduce(transition = thirdTransition),
        )

        val actual = reduce.transition as TestTransitionComposite
        assertThat(actual.head, sameInstance(firstTransition))
        assertThat(actual.tails, equalTo(listOf(secondTransition, thirdTransition)))
    }

    @Test
    fun `Given 2 effects, When composed as reduce, Then result has all effects`() {
        val firstEffect: TestEffect = mockk()
        val secondEffect: TestEffect = mockk()

        val reduce = composeReduce(
            TestReduce(effect = firstEffect),
            TestReduce(effect = secondEffect)
        )

        val actual = reduce.effect as TestEffectComposite
        assertThat(actual.head, sameInstance(firstEffect))
        assertThat(actual.tails, equalTo(listOf(secondEffect)))
    }
}