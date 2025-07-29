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

package com.nlab.statekit.dsl.reduce

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class ReduceBuilderDelegateTest {
    @Test
    fun `Given transition builder, When build transition, Then return correct transition from builder`() {
        val expectedTransition: TestDslTransitionNode = mockk()
        val transitionBuilder: DslTransitionBuilder = mockk {
            every { build() } returns expectedTransition
        }
        val delegate = ReduceBuilderDelegate(transitionBuilder = transitionBuilder, effectBuilder = mockk())
        val actualTransition = delegate.buildTransition()

        assertThat(actualTransition, sameInstance(expectedTransition))
    }

    @Test
    fun `Given effect builder, When build effect, Then return dsl effect from effect builder`() {
        val expectedEffect: TestDslEffectNode = mockk()
        val effectBuilder: DslEffectBuilder = mockk {
            every { build() } returns expectedEffect
        }
        val delegate = ReduceBuilderDelegate(transitionBuilder = mockk(), effectBuilder = effectBuilder)
        val actualEffect = delegate.buildEffect()

        assertThat(actualEffect, sameInstance(expectedEffect))
    }

    @Test
    fun `Given transition block, When add transition node, Then block added to transition builder`() {
        val block = { scope: TestDslTransitionScope -> scope.current }
        val transitionBuilder: DslTransitionBuilder = mockk(relaxed = true)
        val delegate = ReduceBuilderDelegate(transitionBuilder = transitionBuilder, effectBuilder = mockk())
        delegate.addTransitionNode(block)
        verify(exactly = 1) {
            transitionBuilder.addNode(block)
        }
    }

    @Test
    fun `Given effect block, When add effect node, Then block added to effect builder`() {
        val block = { scope: TestDslEffectScope -> }
        val effectBuilder: DslEffectBuilder = mockk(relaxed = true)
        val delegate = ReduceBuilderDelegate(transitionBuilder = mockk(), effectBuilder = effectBuilder)
        delegate.addEffectNode(block)
        verify(exactly = 1) {
            effectBuilder.addNode(block)
        }
    }

    @Test
    fun `Given suspend effect block, When add suspend effect node, Then block added to effect builder`() {
        val block: suspend (TestDslSuspendEffectScope) -> Unit = {}
        val effectBuilder: DslEffectBuilder = mockk(relaxed = true)
        val delegate = ReduceBuilderDelegate(transitionBuilder = mockk(), effectBuilder = effectBuilder)
        delegate.addSuspendEffectNode(block)
        verify(exactly = 1) {
            effectBuilder.addSuspendNode(block)
        }
    }

    @Test
    fun `Given delegate already set up, When add scope, Then delegate properties added to builders`() {
        val expectedTransition: TestDslTransitionNode = mockk()
        val expectedEffect: TestDslEffectNode = mockk()
        val childDelegate: ReduceBuilderDelegate = mockk {
            every { buildTransition() } returns expectedTransition
            every { buildEffect() } returns expectedEffect
        }

        val transitionBuilder: DslTransitionBuilder = mockk(relaxed = true)
        val effectBuilder: DslEffectBuilder = mockk(relaxed = true)
        val delegate = ReduceBuilderDelegate(transitionBuilder = transitionBuilder, effectBuilder = effectBuilder)
        delegate.addScope(childDelegate)

        verify(exactly = 1) {
            transitionBuilder.addTransition(expectedTransition)
        }
        verify(exactly = 1) {
            effectBuilder.addEffect(expectedEffect)
        }
    }

    @Test
    fun `Given empty delegate, When add scope, Then builders never added anything`() {
        val childDelegate: ReduceBuilderDelegate = mockk {
            every { buildTransition() } returns null
            every { buildEffect() } returns null
        }

        val transitionBuilder: DslTransitionBuilder = mockk(relaxed = true)
        val effectBuilder: DslEffectBuilder = mockk(relaxed = true)
        val delegate = ReduceBuilderDelegate(transitionBuilder = transitionBuilder, effectBuilder = effectBuilder)
        delegate.addScope(childDelegate)

        verify(inverse = true) {
            transitionBuilder.addTransition(any())
        }
        verify(inverse = true) {
            effectBuilder.addEffect(any())
        }
    }

    @Test
    fun `Given delegate and predicate, When add predicate scope, Then builders add as predicateScope`() {
        val expectedTransition: TestDslTransitionNode = mockk()
        val expectedEffect: TestDslEffectNode = mockk()
        val childDelegate: ReduceBuilderDelegate = mockk {
            every { buildTransition() } returns expectedTransition
            every { buildEffect() } returns expectedEffect
        }
        val predicate: (TestUpdateSource) -> Boolean = mockk()

        val transitionBuilder: DslTransitionBuilder = mockk(relaxed = true)
        val effectBuilder: DslEffectBuilder = mockk(relaxed = true)
        val delegate = ReduceBuilderDelegate(transitionBuilder = transitionBuilder, effectBuilder = effectBuilder)
        delegate.addPredicateScope(isMatch = predicate, child = childDelegate)

        verify(exactly = 1) {
            transitionBuilder.addPredicateScope(isMatch = predicate, transition = expectedTransition)
        }
        verify(exactly = 1) {
            effectBuilder.addPredicateScope(isMatch = predicate, effect = expectedEffect)
        }
    }

    @Test
    fun `Given delegate and transformer, When add transform scope, Then builders add as transformSourceScope`() {
        val expectedTransition: TestDslTransitionNode = mockk()
        val expectedEffect: TestDslEffectNode = mockk()
        val childDelegate: ReduceBuilderDelegate = mockk {
            every { buildTransition() } returns expectedTransition
            every { buildEffect() } returns expectedEffect
        }
        val transformer: (TestUpdateSource) -> TestUpdateSource = mockk()

        val transitionBuilder: DslTransitionBuilder = mockk(relaxed = true)
        val effectBuilder: DslEffectBuilder = mockk(relaxed = true)
        val delegate = ReduceBuilderDelegate(transitionBuilder = transitionBuilder, effectBuilder = effectBuilder)

        lateinit var generatedSubScope: Any
        delegate.addTransformSourceScope(transformSource = transformer, child = { subScope ->
            generatedSubScope = subScope
            childDelegate
        })

        verify(exactly = 1) {
            transitionBuilder.addTransformSourceScope(
                subScope = generatedSubScope,
                transformSource = transformer,
                transition = expectedTransition
            )
        }
        verify(exactly = 1) {
            effectBuilder.addTransformSourceScope(
                subScope = generatedSubScope,
                transformSource = transformer,
                effect = expectedEffect
            )
        }
    }
}