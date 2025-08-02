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

import com.nlab.statekit.dsl.TestAction
import com.nlab.statekit.dsl.TestState
import com.nlab.statekit.test.reduce.expectedNextState
import com.nlab.statekit.test.reduce.expectedNotChanged
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author Doohyun
 */
class StateScopeReduceBuilderTest {
    @Test
    fun `Given action1, When register transition that changes with action1, Then return correct state`() = runTest {
        val inputAction = TestAction.Action1
        val inputState = TestState.State1
        val expectedState = TestState.State2
        val reduceBuilder = TestStateScopeReduceBuilder {
            transition<TestAction.Action1> { expectedState }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.toReduceTestBuilder()
            .givenCurrent(inputState)
            .actionToDispatch(inputAction)
            .transitionScenario()
            .expectedNextState(expectedState)
    }

    @Test
    fun `Given action2, When register transition that changes with action1, Then state never changed`() = runTest {
        val inputAction = TestAction.Action2
        val reduceBuilder = TestStateScopeReduceBuilder {
            transition<TestAction.Action1> { error("Should not be called") }
        }
        val transition = checkNotNull(reduceBuilder.delegate.buildTransition())
        transition.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(inputAction)
            .transitionScenario()
            .expectedNotChanged()
    }

    @Test
    fun `Given action1, When register effect on action1, Then runner invoked`() = runTest {
        val inputAction = TestAction.Action1
        val runner: () -> Unit = mockk(relaxed = true)
        val reduceBuilder = TestStateScopeReduceBuilder {
            effect<TestAction.Action1> { runner.invoke() }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(inputAction)
            .effectScenario()
            .launchAndGetTrace()
        verify(exactly = 1) {
            runner.invoke()
        }
    }

    @Test
    fun `Given action2, When register effect on action1, Then runner never invoked`() = runTest {
        val inputAction = TestAction.Action2
        val runner: () -> Unit = mockk()
        val reduceBuilder = TestStateScopeReduceBuilder {
            effect<TestAction.Action1> { runner.invoke() }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(inputAction)
            .effectScenario()
            .launchAndGetTrace()
        verify(inverse = true) {
            runner.invoke()
        }
    }

    @Test
    fun `Given action1, When register suspend effect on action1, Then runner invoked`() = runTest {
        val inputAction = TestAction.Action1
        val runner: suspend () -> Unit = mockk(relaxed = true)
        val reduceBuilder = TestStateScopeReduceBuilder {
            suspendEffect<TestAction.Action1> { runner.invoke() }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(inputAction)
            .effectScenario()
            .launchAndGetTrace()
        coVerify(exactly = 1) {
            runner.invoke()
        }
    }

    @Test
    fun `Given matched predicate, When register effect in scope with predicate, Then runner invoked`() = runTest {
        val predicate: (TestUpdateSource) -> Boolean = { true }
        val runner: () -> Unit = mockk(relaxed = true)
        val reduceBuilder = TestStateScopeReduceBuilder {
            scope(isMatch = predicate) {
                effect { runner.invoke() }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace()
        verify(exactly = 1) {
            runner.invoke()
        }
    }

    @Test
    fun `Given transformSource, When register effect in scope with transformSource, Then runner invoked`() = runTest {
        val transformSource: (TestUpdateSource) -> UpdateSource<Int, TestState>? = {
            UpdateSource(action = 1, current = it.current)
        }
        val runner: () -> Unit = mockk(relaxed = true)
        val reduceBuilder = TestStateScopeReduceBuilder {
            scope(transformSource = transformSource) {
                effect { runner.invoke() }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.toReduceTestBuilder()
            .givenCurrent(TestState.genState())
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace()
        verify(exactly = 1) {
            runner.invoke()
        }
    }

    @Test
    fun `Given state1 input, When register effect in scope with state1 filter, Then runner invoked`() = runTest {
        val input = TestState.State1
        val runner: () -> Unit = mockk(relaxed = true)
        val reduceBuilder = TestStateScopeReduceBuilder {
            scope<TestState.State1> {
                effect { runner.invoke() }
            }
        }
        val effect = checkNotNull(reduceBuilder.delegate.buildEffect())
        effect.toReduceTestBuilder()
            .givenCurrent(input)
            .actionToDispatch(TestAction.genAction())
            .effectScenario()
            .launchAndGetTrace()
        verify(exactly = 1) {
            runner.invoke()
        }
    }
}

@Suppress("TestFunctionName")
private fun TestStateScopeReduceBuilder(
    buildDSL: StateScopeReduceBuilder<TestAction, TestState, TestAction, TestState>.() -> Unit
) = StateScopeReduceBuilder<TestAction, TestState, TestAction, TestState>(scope = Any()).apply(buildDSL)