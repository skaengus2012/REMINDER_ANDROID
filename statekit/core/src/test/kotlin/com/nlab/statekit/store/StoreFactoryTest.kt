package com.nlab.statekit.store

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import com.nlab.statekit.bootstrap.Bootstrap
import com.nlab.statekit.bootstrap.EmptyBootstrap
import com.nlab.statekit.reduce.NodeStackPool
import com.nlab.statekit.reduce.Reduce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
class StoreFactoryTest {
    @Test
    fun `Given initState, When create store, Then store has initState`() = runTest {
        val initState = TestState.genState()
        val store = createStoreFromStoreFactory(initState = initState)

        assertThat(store.state.value, equalTo(initState))
    }

    @Test
    fun `When create store, Then bootstrap fetched`() = runTest {
        val initState = TestState.genState()
        val bootstrap: Bootstrap<TestAction> = mock()
        createStoreFromStoreFactory(initState = initState, bootstrap = bootstrap)

        verify(bootstrap, once()).fetch(any(), any(), any())
    }
}

private fun TestScope.createStoreFromStoreFactory(
    coroutineScope: CoroutineScope = this,
    initState: TestState = TestState.genState(),
    reduce: Reduce<TestAction, TestState> = Reduce(),
    bootstrap: Bootstrap<TestAction> = EmptyBootstrap()
): Store<TestAction, TestState> = StoreFactory(NodeStackPool()).create(
    coroutineScope,
    initState,
    reduce,
    bootstrap
)