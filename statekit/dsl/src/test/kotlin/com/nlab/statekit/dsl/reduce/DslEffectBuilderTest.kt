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

import com.nlab.testkit.faker.genInt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.instanceOf
import org.junit.Test

/**
 * @author Doohyun
 */
class DslEffectBuilderTest {
    @Test
    fun `When build without add, Then return null`() {
        val effectBuilder = DslEffectBuilder(scope = Any())
        val effect = effectBuilder.build()
        assertThat(effect, nullValue())
    }

    @Test
    fun `Given effect, When build after addEffect, Then return added effect`() {
        val scope = "1"
        val expectedEffect = TestDslEffect(scope = scope)
        val effectBuilder = DslEffectBuilder(scope = Any())
        effectBuilder.addEffect(expectedEffect)

        assertThat(effectBuilder.build(), sameInstance(expectedEffect))
    }

    @Test
    fun `Given 2 or more effects, When build after add all, Then return composite`() {
        val scope = Any()
        val expectedEffects = List(genInt(min = 2, max = 5)) { TestDslEffect(scope = scope) }
        val effectBuilder = DslEffectBuilder(scope)
        expectedEffects.forEach { effectBuilder.addEffect(it) }

        val actualTransition = effectBuilder.build() as DslEffect.Composite
        assertThat(actualTransition.effects, equalTo(expectedEffects))
    }

    @Test
    fun `Given scope to result block, When build after addNode, Then return node`() {
        val block: (TestDslEffectScope) -> Unit = {}
        val effectBuilder = DslEffectBuilder(scope = Any())
        effectBuilder.addNode(block)

        assertThat(effectBuilder.build(), instanceOf(DslEffect.Node::class))
    }

    @Test
    fun `Given scope to result block, When build after addSuspendNode, Then return suspend node`() {
        val block: (TestDslSuspendEffectScope) -> Unit = {}
        val effectBuilder = DslEffectBuilder(scope = Any())
        effectBuilder.addSuspendNode(block)

        assertThat(effectBuilder.build(), instanceOf(DslEffect.SuspendNode::class))
    }

    @Test
    fun `Given predicate and effect, When build after addPredicateScope, Then return predicateScope`() {
        val scope = Any()
        val isMatch: (TestUpdateSource) -> Boolean = { false }
        val effect = TestDslEffect(scope)

        val effectBuilder = DslEffectBuilder(scope)
        effectBuilder.addPredicateScope(isMatch, effect)

        assertThat(effectBuilder.build(), instanceOf(DslEffect.PredicateScope::class))
    }

    @Test
    fun `Given subScope, transformSource block and effect, When build after addTransformSourceScope, Then return TransformSourceScope`() {
        val scope = Any()
        val subScope = Any()
        val transformSource: (TestUpdateSource) -> TestUpdateSource = { it }
        val effect = TestDslEffect(subScope)

        val effectBuilder = DslEffectBuilder(scope)
        effectBuilder.addTransformSourceScope(
            subScope,
            transformSource,
            effect
        )

        assertThat(effectBuilder.build(), instanceOf(DslEffect.TransformSourceScope::class))
    }
}