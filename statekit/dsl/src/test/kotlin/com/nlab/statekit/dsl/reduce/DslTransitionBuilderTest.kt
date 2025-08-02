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
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genBothify
import com.nlab.testkit.faker.genInt
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class DslTransitionBuilderTest {
    @Test
    fun `When build without add, Then return null`() {
        val transitionBuilder = DslTransitionBuilder(scope = Any())
        val transition = transitionBuilder.build()
        assertThat(transition, nullValue())
    }

    @Test
    fun `Given transition, When build after addTransition, Then return added transition`() {
        val rootScope = genBothify()
        val expectedTransition = TestDslTransitionNode(rootScope) { it.current }
        val transitionBuilder = DslTransitionBuilder(rootScope)
        transitionBuilder.addTransition(expectedTransition)

        assertThat(transitionBuilder.build(), sameInstance(expectedTransition))
    }

    @Test
    fun `Given 2 or more transitions, When build after add all, Then return correct composite`() {
        val rootScope = genBothify()
        val expectedTransitions = List(genInt(min = 2, max = 5)) {
            TestDslTransitionNode(rootScope) { it.current }
        }
        val transitionBuilder = DslTransitionBuilder(rootScope)
        expectedTransitions.forEach { transitionBuilder.addTransition(it) }

        val actualTransitions = buildList {
            val composite = transitionBuilder.build() as DslTransition.Composite
            add(composite.head)
            addAll(composite.tails)
        }

        assertThat(actualTransitions, equalTo(expectedTransitions))
    }

    @Test
    fun `Given scope to result block, When build after addNode, Then return correct transition`() {
        val rootScope = genBothify()
        val block: (TestDslTransitionScope) -> TestState = { it.current }
        val transitionBuilder = DslTransitionBuilder(scope = rootScope)
        transitionBuilder.addNode(block)

        val actual = transitionBuilder.build()
        val actualNode = actual as DslTransition.Node<*, *, *>
        assertThat(actualNode.scope, equalTo(rootScope))
        assertThat(actualNode.next, sameInstance(block))
    }

    @Test
    fun `Given predicate and transition, When build after addPredicateScope, Then return correct transition`() {
        val rootScope = genBothify()
        val isMatch: (TestUpdateSource) -> Boolean = { genBoolean() }
        val childTransition = TestDslTransitionNode(rootScope) { it.current }

        val transitionBuilder = DslTransitionBuilder(rootScope)
        transitionBuilder.addPredicateScope(isMatch, childTransition)

        val actual = transitionBuilder.build()
        val actualPredicateScope = actual as DslTransition.PredicateScope<*, *>
        assertThat(actualPredicateScope.scope, equalTo(rootScope))
        assertThat(actualPredicateScope.transition, sameInstance(childTransition))
    }


    @Test
    fun `Given subScope, transformSource, transition, When build after addTransformSourceScope, Then return correct transition`() {
        val rootScope = "1"
        val subScope = "2"
        val transformSource: (TestUpdateSource) -> TestUpdateSource = { it }
        val childTransition = TestDslTransitionNode(subScope) { it.current }

        val transitionBuilder = DslTransitionBuilder(rootScope)
        transitionBuilder.addTransformSourceScope(
            subScope,
            transformSource,
            childTransition
        )

        val actual = transitionBuilder.build()
        val actualTransformScope = actual as DslTransition.TransformSourceScope<*, *, *, *>

        assertThat(actualTransformScope.scope, equalTo(rootScope))
        assertThat(actualTransformScope.subScope, equalTo(subScope))
        assertThat(actualTransformScope.transformSource, sameInstance(transformSource))
        assertThat(actualTransformScope.transition, sameInstance(childTransition))
    }
}