package com.nlab.statekit.store

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import com.nlab.statekit.reduce.EmptyReduce
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * @author Thalys
 */
class StoreFactoriesKtTest {
    @Test
    fun successCreateStore() {
        createStore<TestAction, TestState>(coroutineScope = mock(), initState = TestState.genState())
    }

    @Test
    fun successCreateComponentStore() {
        createComponentStore<TestAction, TestState>(initState = TestState.genState(), reduce = EmptyReduce())
    }
}