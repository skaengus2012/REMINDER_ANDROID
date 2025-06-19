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
    fun `Given transition builder, When build transition, Then return dsl transition from transition builder`() {
        val dslTransition = TestDslTransition()
        val transitionBuilder: DslTransitionBuilder = mockk {
            every { build() } returns dslTransition
        }
        val delegate = ReduceBuilderDelegate(transitionBuilder = transitionBuilder, effectBuilder = mockk())
        assertThat(delegate.buildTransition(), sameInstance(dslTransition))
    }

    @Test
    fun `Given effect builder, When build effect, Then return dsl effect from effect builder`() {
        val dslEffect = TestDslEffect()
        val effectBuilder: DslEffectBuilder = mockk {
            every { build() } returns dslEffect
        }
        val delegate = ReduceBuilderDelegate(transitionBuilder = mockk(), effectBuilder = effectBuilder)
        assertThat(delegate.buildEffect(), sameInstance(dslEffect))
    }

    @Test
    fun `Given transition builder and block, When add transition node, Then block added to transition builder`() {
        val block = { scope: TestDslTransitionScope -> scope.current }
        val transitionBuilder: DslTransitionBuilder = mockk(relaxed = true)
        val delegate = ReduceBuilderDelegate(transitionBuilder = transitionBuilder, effectBuilder = mockk())

        delegate.addTransitionNode(block)
        verify(exactly = 1) { transitionBuilder.addNode(block) }
    }

    @Test
    fun `Given effect builder and block, When add effect node, Then block added to effect builder`() {
        val block: (TestDslEffectScope) -> Unit = {}
        val effectBuilder: DslEffectBuilder = mockk(relaxed = true)
        val delegate = ReduceBuilderDelegate(transitionBuilder = mockk(), effectBuilder = effectBuilder)

        delegate.addEffectNode(block)
        verify(exactly = 1) { effectBuilder.addNode(block) }
    }

    @Test
    fun `Given suspend effect builder and block, When add suspend effect node, Then block added to effect builder`() {
        val block: suspend (TestDslSuspendEffectScope) -> Unit = {}
        val effectBuilder: DslEffectBuilder = mockk(relaxed = true)
        val delegate = ReduceBuilderDelegate(transitionBuilder = mockk(), effectBuilder = effectBuilder)

        delegate.addSuspendEffectNode(block)
        verify(exactly = 1) { effectBuilder.addSuspendNode(block) }
    }

    @Test
    fun `Given transition, effect, delegate that return transition and effect, transition builder and effect builder, When add scope, Then transition, builder added to builder`() {
        val dslTransition = TestDslTransition()
        val dslEffect = TestDslEffect()
        val from: ReduceBuilderDelegate = mockk {
            every { buildTransition() } returns dslTransition
            every { buildEffect() } returns dslEffect
        }
        val transitionBuilder: DslTransitionBuilder = mockk(relaxed = true)
        val effectBuilder: DslEffectBuilder = mockk(relaxed = true)
        val delegate = ReduceBuilderDelegate(transitionBuilder, effectBuilder)
        delegate.addScope(from)

        verify(exactly = 1) { transitionBuilder.addTransition(dslTransition) }
        verify(exactly = 1) { effectBuilder.addEffect(dslEffect) }
    }

    @Test
    fun `Given delegate that return null transition and effect transition builder and effect builder, When add scope, Then builders never called`() {
        val from: ReduceBuilderDelegate = mockk {
            every { buildTransition() } returns null
            every { buildEffect() } returns null
        }
        val transitionBuilder: DslTransitionBuilder = mockk()
        val effectBuilder: DslEffectBuilder = mockk()
        val delegate = ReduceBuilderDelegate(transitionBuilder, effectBuilder)
        delegate.addScope(from)

        verify(inverse = true) { transitionBuilder.addTransition(any()) }
        verify(inverse = true) { effectBuilder.addEffect(any()) }
    }

    @Test
    fun `Given transition, effect, delegate that return transition and effect, predicate, transition builder and effect builder, When add predicate scope, Then transition, builder added to builder`() {
        val dslTransition = TestDslTransition()
        val dslEffect = TestDslEffect()
        val from: ReduceBuilderDelegate = mockk {
            every { buildTransition() } returns dslTransition
            every { buildEffect() } returns dslEffect
        }
        val predicate = { _: TestUpdateSource -> false }
        val transitionBuilder: DslTransitionBuilder = mockk(relaxed = true)
        val effectBuilder: DslEffectBuilder = mockk(relaxed = true)
        val delegate = ReduceBuilderDelegate(transitionBuilder, effectBuilder)
        delegate.addPredicateScope(isMatch = predicate, from = from)

        verify(exactly = 1) { transitionBuilder.addPredicateScope(predicate, dslTransition) }
        verify(exactly = 1) { effectBuilder.addPredicateScope(predicate, dslEffect) }
    }

    @Test
    fun `Given transition, effect, delegate that return transition and effect, transform source function, transition builder and effect builder, When add transform scope, Then transition, builder added to builder`() {
        val dslTransition = TestDslTransition()
        val dslEffect = TestDslEffect()
        val from: ReduceBuilderDelegate = mockk {
            every { buildTransition() } returns dslTransition
            every { buildEffect() } returns dslEffect
        }
        val transformSource: (TestUpdateSource) -> TestUpdateSource = { it }
        val transitionBuilder: DslTransitionBuilder = mockk(relaxed = true)
        val effectBuilder: DslEffectBuilder = mockk(relaxed = true)
        val delegate = ReduceBuilderDelegate(transitionBuilder, effectBuilder)
        lateinit var subScope: Any
        delegate.addTransformSourceScope(transformSource) {
            subScope = it
            from
        }

        verify(exactly = 1) { transitionBuilder.addTransformSourceScope(subScope, transformSource, dslTransition) }
        verify(exactly = 1) { effectBuilder.addTransformSourceScope(subScope, transformSource, dslEffect) }
    }
}