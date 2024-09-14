package com.nlab.statekit.reduce.dsl

import com.nlab.statekit.TestAction
import com.nlab.statekit.TestState
import org.junit.Test

/**
 * @author Thalys
 */
class DslTransitionScopeTest {
    @Test
    fun successCreateDslTransitionScope() {
        DslTransitionScope(UpdateSource(TestAction.genAction(), TestState.genState()))
    }
}