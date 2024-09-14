package com.nlab.statekit.bootstrap.flow

import com.nlab.statekit.TestAction
import kotlinx.coroutines.flow.Flow
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.instanceOf
import org.junit.Test
import org.mockito.kotlin.mock

/**
 * @author Doohyun
 */
internal class FlowBootstrapFactoriesKtTest {
    @Test
    fun `Given action stream, When constructed, Then return shared bootstrap`() {
        val flow: Flow<TestAction> = mock()
        val bootstrap = flow.bootstrap()
        assertThat(bootstrap, instanceOf(SharedBootstrap::class))
    }
}