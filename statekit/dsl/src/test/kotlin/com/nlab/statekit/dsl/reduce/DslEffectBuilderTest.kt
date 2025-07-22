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

import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genBothify
import com.nlab.testkit.faker.genInt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
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
        val rootScope = genBothify()
        val expectedEffect = TestDslEffectNode(scope = rootScope) {}
        val effectBuilder = DslEffectBuilder(scope = rootScope)
        effectBuilder.addEffect(expectedEffect)

        assertThat(effectBuilder.build(), sameInstance(expectedEffect))
    }

    @Test
    fun `Given 2 or more effects, When build after add all, Then return correct composite`() {
        val rootScope = genBothify()
        val expectedEffects = List(genInt(min = 2, max = 5)) {
            TestDslEffectNode(scope = rootScope) {}
        }
        val effectBuilder = DslEffectBuilder(rootScope)
        expectedEffects.forEach { effectBuilder.addEffect(it) }

        val actualEffects = buildList {
            val composite = effectBuilder.build() as DslEffect.Composite
            add(composite.head)
            addAll(composite.tails)
        }
        assertThat(actualEffects, equalTo(expectedEffects))
    }

    @Test
    fun `Given scope to result block, When build after addNode, Then return correct effect`() {
        val rootScope = genBothify()
        val block: (TestDslEffectScope) -> Unit = {}
        val effectBuilder = DslEffectBuilder(scope = rootScope)
        effectBuilder.addNode(block)

        val actual = effectBuilder.build()
        val actualNode = actual as DslEffect.Node<*, *>

        assertThat(actualNode.scope, equalTo(rootScope))
        assertThat(actualNode.invoke, sameInstance(block))
    }

    @Test
    fun `Given scope to result block, When build after addSuspendNode, Then return correct effect`() {
        val rootScope = genBothify()
        val block: suspend (TestDslSuspendEffectScope) -> Unit = {}
        val effectBuilder = DslEffectBuilder(scope = rootScope)
        effectBuilder.addSuspendNode(block)

        val actual = effectBuilder.build()
        val actualNode = actual as DslEffect.SuspendNode<*, *, *>

        assertThat(actualNode.scope, equalTo(rootScope))
        assertThat(actualNode.invoke, sameInstance(block))
    }

    @Test
    fun `Given predicate and effect, When build after addPredicateScope, Then return correct effect`() {
        val rootScope = genBothify()
        val isMatch: (TestUpdateSource) -> Boolean = { genBoolean() }
        val childEffect = TestDslEffectNode(rootScope) {}

        val effectBuilder = DslEffectBuilder(rootScope)
        effectBuilder.addPredicateScope(isMatch, childEffect)

        val actual = effectBuilder.build()
        val actualPredicateScope = actual as DslEffect.PredicateScope<*, *>

        assertThat(actualPredicateScope.scope, equalTo(rootScope))
        assertThat(actualPredicateScope.effect, sameInstance(childEffect))
    }

    @Test
    fun `Given subScope, transformSource block and effect, When build after addTransformSourceScope, Then return correct effect`() {
        val rootScope = "1"
        val subScope = "2"
        val transformSource: (TestUpdateSource) -> TestUpdateSource = { it }
        val childEffect = TestDslEffectNode(subScope) {}

        val effectBuilder = DslEffectBuilder(rootScope)
        effectBuilder.addTransformSourceScope(
            subScope,
            transformSource,
            childEffect
        )

        val actual = effectBuilder.build()
        val actualEffectScope = actual as DslEffect.TransformSourceScope<*, *, *, *>

        assertThat(actualEffectScope.scope, equalTo(rootScope))
        assertThat(actualEffectScope.subScope, equalTo(subScope))
        assertThat(actualEffectScope.transformSource, sameInstance(transformSource))
        assertThat(actualEffectScope.effect, sameInstance(childEffect))
    }
}