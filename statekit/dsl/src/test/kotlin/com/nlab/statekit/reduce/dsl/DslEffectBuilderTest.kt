package com.nlab.statekit.reduce.dsl

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

private typealias TestDslEffectBuilder = DslEffectBuilder<TestAction, TestState, TestState>

/**
 * @author Doohyun
 */
class DslEffectBuilderTest {
    /**
    @Test
    fun `Given multiple effects, When build, Then return async runnable composition effect`() = runTest {
        val firstEffect: () -> Unit = mock()
        val secondEffect: () -> Unit = mock()
        val compositionEffect = TestDslEffectBuilder()
            .apply {
                add {
                    delay(1_000)
                    firstEffect()
                }
                add {
                    delay(1_000)
                    secondEffect()
                }
            }
            .build()
        compositionEffect.invoke(
            DslEffectScope(
                UpdateSource(TestAction.genAction(), TestState.genState()),
                mock()
            )
        )
        advanceTimeBy(1_500)
        verify(firstEffect, once()).invoke()
        verify(secondEffect, once()).invoke()
    }*/
}