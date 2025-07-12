/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.statekit.store

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import com.nlab.statekit.reduce.NodeStackPool
import com.nlab.statekit.reduce.Reduce
import com.nlab.statekit.reduce.TestEffect
import com.nlab.statekit.reduce.TestEffectComposite
import com.nlab.statekit.reduce.TestEffectNode
import com.nlab.statekit.reduce.TestEffectSuspendNode
import com.nlab.statekit.reduce.TestReduce
import com.nlab.statekit.reduce.TestTransitionNode
import com.nlab.statekit.reduce.launch
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.unconfinedTestDispatcher
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class RootActionDispatcherTest {
    @Test
    fun `Given reduce without transition, When dispatch, Then baseState never changed`() = runTest {
        val initState = TestState.genState()
        val baseState = MutableStateFlow(initState)
        val reduce = TestReduce(transition = null)
        val actionDispatcher = RootActionDispatcher(
            reduce = reduce,
            baseState = baseState,
            nodeStackPool = mockk()
        )

        actionDispatcher.dispatch(TestAction.genAction())
        assertThat(initState, equalTo(baseState.value))
    }

    @Test
    fun `Given reduce with changeable transition, When dispatch, Then baseState changed`() = runTest {
        val initState = TestState.State1
        val changedState = TestState.State2
        val baseState = MutableStateFlow<TestState>(initState)
        val reduce = TestReduce(
            transition = TestTransitionNode { _, _ -> changedState }
        )
        val actionDispatcher = RootActionDispatcher(
            reduce = reduce,
            baseState = baseState,
            nodeStackPool = NodeStackPool()
        )
        actionDispatcher.dispatch(TestAction.genAction())
        assertThat(changedState, equalTo(baseState.value))
    }

    @Test
    fun `Given reduce with transition and effect, When dispatch, Then effect launch with initState`() = runTest {
        mockkStatic(TestEffect::launch)
        val initState = TestState.State1
        val changedState = TestState.State2
        val effect: TestEffect = mockk {
            every { launch(any(), any(), any(), any()) } just Runs
        }
        val reduce = TestReduce(
            transition = TestTransitionNode { _, _ -> changedState },
            effect = effect
        )
        val actionDispatcher = RootActionDispatcher(
            reduce = reduce,
            baseState = MutableStateFlow(initState),
            nodeStackPool = NodeStackPool()
        )
        actionDispatcher.dispatch(TestAction.genAction())

        verify(exactly = 1) {
            effect.launch(any(), eq(initState), any(), any())
        }
        unmockkStatic(TestEffect::launch)
    }

    @Test
    fun `Given reduce with transition by action3, action1 to action3 effect, When dispatch action1, Then baseState changed`() = runTest {
        val initState = TestState.State1
        val changedState = TestState.State2
        val reduce = Reduce(
            transition = TestTransitionNode { action, state ->
                if (action == TestAction.Action3) changedState
                else state
            },
            effect = TestEffectSuspendNode { action, state, actionDispatcher ->
                if (action == TestAction.Action1) {
                    actionDispatcher.dispatch(TestAction.Action3)
                }
            }
        )
        val baseState = MutableStateFlow<TestState>(initState)
        val actionDispatcher = RootActionDispatcher(
            reduce = reduce,
            baseState = baseState,
            nodeStackPool = NodeStackPool()
        )

        actionDispatcher.dispatch(TestAction.Action1)
        assertThat(changedState, equalTo(baseState.value))
    }

    @Test
    fun `Given reduce with throwable effects, currentScope has handler, When dispatch, Then all throwable invoked handler`() = runTest {
        val exceptions = List(size = 3) { RuntimeException() }
        val runner: (Throwable) -> Unit = mockk(relaxed = true)
        val reduce = Reduce(
            effect = TestEffectComposite(
                head = TestEffectNode { _, _, ->
                    throw exceptions[0]
                },
                tails = listOf(
                    TestEffectSuspendNode { _, _, _ ->
                        throw exceptions[1]
                    },
                    TestEffectNode { _, _ ->
                        throw exceptions[2]
                    },
                )
            )
        )
        val actionDispatcher = RootActionDispatcher(
            reduce = reduce,
            baseState = MutableStateFlow(TestState.genState()),
            nodeStackPool = NodeStackPool()
        )
        launch(CoroutineExceptionHandler { _, throwable -> runner.invoke(throwable) }) {
            actionDispatcher.dispatch(TestAction.genAction())
        }
        advanceUntilIdle()

        exceptions.forEach {
            verify(exactly = 1) { runner.invoke(it) }
        }
    }

    @Test
    fun `Given reduce with throwable effects, currentScope hasn't handler, When dispatch, Then first error thrown`() = runTest {
        val exceptions = List(size = 3) { RuntimeException() }
        val expectedException = exceptions[0]
        val reduce = Reduce(
            effect = TestEffectComposite(
                // To anticipate an exception, you need to run a synchronous effect.
                head = TestEffectNode { _, _ ->
                    throw exceptions[0]
                },
                tails = listOf(
                    TestEffectNode { _, _ ->
                        throw exceptions[1]
                    },
                    TestEffectNode { _, _ ->
                        throw exceptions[2]
                    },
                )
            )
        )
        val actionDispatcher = RootActionDispatcher(
            reduce = reduce,
            baseState = MutableStateFlow(TestState.genState()),
            nodeStackPool = NodeStackPool()
        )

        lateinit var actualThrowable: Throwable
        // TestScope has a built-in exceptionHandler, so create a new coroutineScope and use it.
        CoroutineScope(unconfinedTestDispatcher).launch {
            try {
                actionDispatcher.dispatch(TestAction.genAction())
            } catch (e: Exception) {
                actualThrowable = e
            }
        }
        advanceUntilIdle()

        assertThat(actualThrowable, equalTo(expectedException))
    }
}