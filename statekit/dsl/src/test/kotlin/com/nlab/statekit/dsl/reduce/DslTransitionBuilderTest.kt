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

import com.nlab.statekit.dsl.TestState
import com.nlab.testkit.faker.genInt
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.instanceOf
import org.junit.Test

/**
 * @author Thalys
 */
class DslTransitionBuilderTest {
    /**
    @Test
    fun `When build without add, Then return null`() {
        val transitionBuilder = DslTransitionBuilder(scope = Any())
        val transition = transitionBuilder.build()
        assertThat(transition, nullValue())
    }

    @Test
    fun `Given transition, When build after addTransition, Then return added transition`() {
        val scope = "1"
        val expectedTransition = TestDslTransition(scope)
        val transitionBuilder = DslTransitionBuilder(scope)
        transitionBuilder.addTransition(expectedTransition)

        assertThat(transitionBuilder.build(), sameInstance(expectedTransition))
    }

    @Test
    fun `Given 2 or more transitions, When build after add all, Then return composite`() {
        val scope = Any()
        val expectedTransitions = List(genInt(min = 2, max = 5)) { TestDslTransition(scope = scope) }
        val transitionBuilder = DslTransitionBuilder(scope)
        expectedTransitions.forEach { transitionBuilder.addTransition(it) }

        val actualTransition = transitionBuilder.build() as DslTransition.Composite
        assertThat(actualTransition.transitions, equalTo(expectedTransitions))
    }

    @Test
    fun `Given scope to result block, When build after addNode, Then return node`() {
        val block: (TestDslTransitionScope) -> TestState = { it.current }
        val transitionBuilder = DslTransitionBuilder(scope = Any())
        transitionBuilder.addNode(block)

        assertThat(transitionBuilder.build(), instanceOf(DslTransition.Node::class))
    }

    @Test
    fun `Given predicate and transition, When build after addPredicateScope, Then return predicateScope`() {
        val scope = Any()
        val isMatch: (TestUpdateSource) -> Boolean = { false }
        val transition = TestDslTransition(scope)

        val transitionBuilder = DslTransitionBuilder(scope)
        transitionBuilder.addPredicateScope(isMatch, transition)

        assertThat(transitionBuilder.build(), instanceOf(DslTransition.PredicateScope::class))
    }

    @Test
    fun `Given subScope, transformSource block and transition, When build after addTransformSourceScope, Then return TransformSourceScope`() {
        val scope = Any()
        val subScope = Any()
        val transformSource: (TestUpdateSource) -> TestUpdateSource = { it }
        val transition = TestDslTransition(subScope)

        val transitionBuilder = DslTransitionBuilder(scope)
        transitionBuilder.addTransformSourceScope(
            subScope,
            transformSource,
            transition
        )

        assertThat(transitionBuilder.build(), instanceOf(DslTransition.TransformSourceScope::class))
    }*/
}