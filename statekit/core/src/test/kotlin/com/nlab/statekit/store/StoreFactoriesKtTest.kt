package com.nlab.statekit.store

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import io.mockk.mockk
import org.junit.Test

/**
 * @author Thalys
 */
class StoreFactoriesKtTest {
    @Test
    fun successCreateStore() {
        createStore<TestAction, TestState>(coroutineScope = mockk(), initState = TestState.genState())
    }
}