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

package com.nlab.statekit.store

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import com.nlab.statekit.reduce.AccumulatorPool
import com.nlab.statekit.reduce.Reduce
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class ComponentStoreFactoryTest {
    @Test
    fun `Given initState, When create store, Then store has initState`() = runTest {
        val initState = TestState.genState()
        val store = createComponentStoreFromStoreFactory(initState = initState)

        assertThat(store.state.value, equalTo(initState))
    }
}

private fun createComponentStoreFromStoreFactory(
    initState: TestState = TestState.genState(),
    reduce: Reduce<TestAction, TestState> = Reduce(),
): ComponentStore<TestAction, TestState> = ComponentStoreFactory(AccumulatorPool()).create(
    initState,
    reduce
)