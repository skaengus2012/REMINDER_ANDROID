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

import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.once
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


/**
 * @author Doohyun
 */
class ReduceBuilderDelegateTest {
    @Test
    fun `Given transition builder, When build transition, Then return dsl transition from transition builder`() {
        val dslTransition = TestDslTransition()
        val transitionBuilder: DslTransitionBuilder = mock {
            whenever(mock.build()) doReturn dslTransition
        }
        val delegate = ReduceBuilderDelegate(transitionBuilder = transitionBuilder, effectBuilder = mock())
        assertThat(delegate.buildTransition(), sameInstance(dslTransition))
    }

    @Test
    fun `Given effect builder, When build effect, Then return dsl effect from effect builder`() {
        val dslEffect = TestDslEffect()
        val effectBuilder: DslEffectBuilder = mock {
            whenever(mock.build()) doReturn dslEffect
        }
        val delegate = ReduceBuilderDelegate(transitionBuilder = mock(), effectBuilder = effectBuilder)
        assertThat(delegate.buildEffect(), sameInstance(dslEffect))
    }

    @Test
    fun `Given transition builder and block, When add transition node, Then block added to transition builder`() {
        val block = { scope: TestDslTransitionScope -> scope.current }
        val transitionBuilder: DslTransitionBuilder = mock()
        val delegate = ReduceBuilderDelegate(transitionBuilder = transitionBuilder, effectBuilder = mock())

        delegate.addTransitionNode(block)
        verify(transitionBuilder, once()).addNode(block)
    }

    @Test
    fun `Given effect builder and block, When add effect node, Then block added to effect builder`() {
        val block: (TestDslEffectScope) -> Unit = {}
        val effectBuilder: DslEffectBuilder = mock()
        val delegate = ReduceBuilderDelegate(transitionBuilder = mock(), effectBuilder = effectBuilder)

        delegate.addEffectNode(block)
        verify(effectBuilder, once()).addNode(block)
    }

    @Test
    fun `Given suspend effect builder and block, When add suspend effect node, Then block added to effect builder`() {
        val block: suspend (TestDslSuspendEffectScope) -> Unit = {}
        val effectBuilder: DslEffectBuilder = mock()
        val delegate = ReduceBuilderDelegate(transitionBuilder = mock(), effectBuilder = effectBuilder)

        delegate.addSuspendEffectNode(block)
        verify(effectBuilder, once()).addSuspendNode(block)
    }

    @Test
    fun `Given transition, effect, delegate that return transition and effect, transition builder and effect builder, When add scope, Then transition, builder added to builder`() {
        val dslTransition = TestDslTransition()
        val dslEffect = TestDslEffect()
        val from: ReduceBuilderDelegate = mock {
            whenever(mock.buildTransition()) doReturn dslTransition
            whenever(mock.buildEffect()) doReturn dslEffect
        }
        val transitionBuilder: DslTransitionBuilder = mock()
        val effectBuilder: DslEffectBuilder = mock()
        val delegate = ReduceBuilderDelegate(transitionBuilder, effectBuilder)
        delegate.addScope(from)

        verify(transitionBuilder, once()).addTransition(dslTransition)
        verify(effectBuilder, once()).addEffect(dslEffect)
    }

    @Test
    fun `Given delegate that return null transition and effect transition builder and effect builder, When add scope, Then builders never called`() {
        val from: ReduceBuilderDelegate = mock {
            whenever(mock.buildTransition()) doReturn null
            whenever(mock.buildEffect()) doReturn null
        }
        val transitionBuilder: DslTransitionBuilder = mock()
        val effectBuilder: DslEffectBuilder = mock()
        val delegate = ReduceBuilderDelegate(transitionBuilder, effectBuilder)
        delegate.addScope(from)

        verify(transitionBuilder, never()).addTransition(any())
        verify(effectBuilder, never()).addEffect(any())
    }

    @Test
    fun `Given transition, effect, delegate that return transition and effect, predicate, transition builder and effect builder, When add predicate scope, Then transition, builder added to builder`() {
        val dslTransition = TestDslTransition()
        val dslEffect = TestDslEffect()
        val from: ReduceBuilderDelegate = mock {
            whenever(mock.buildTransition()) doReturn dslTransition
            whenever(mock.buildEffect()) doReturn dslEffect
        }
        val predicate = { _: TestUpdateSource -> false }
        val transitionBuilder: DslTransitionBuilder = mock()
        val effectBuilder: DslEffectBuilder = mock()
        val delegate = ReduceBuilderDelegate(transitionBuilder, effectBuilder)
        delegate.addPredicateScope(isMatch = predicate, from = from)

        verify(transitionBuilder, once()).addPredicateScope(predicate, dslTransition)
        verify(effectBuilder, once()).addPredicateScope(predicate, dslEffect)
    }

    @Test
    fun `Given transition, effect, delegate that return transition and effect, transform source function, transition builder and effect builder, When add transform scope, Then transition, builder added to builder`() {
        val dslTransition = TestDslTransition()
        val dslEffect = TestDslEffect()
        val from: ReduceBuilderDelegate = mock {
            whenever(mock.buildTransition()) doReturn dslTransition
            whenever(mock.buildEffect()) doReturn dslEffect
        }
        val transformSource: (TestUpdateSource) -> TestUpdateSource = { it }
        val transitionBuilder: DslTransitionBuilder = mock()
        val effectBuilder: DslEffectBuilder = mock()
        val delegate = ReduceBuilderDelegate(transitionBuilder, effectBuilder)
        lateinit var subScope: Any
        delegate.addTransformSourceScope(transformSource) {
            subScope = it
            from
        }

        verify(transitionBuilder, once()).addTransformSourceScope(subScope, transformSource, dslTransition)
        verify(effectBuilder, once()).addTransformSourceScope(subScope, transformSource, dslEffect)
    }
}