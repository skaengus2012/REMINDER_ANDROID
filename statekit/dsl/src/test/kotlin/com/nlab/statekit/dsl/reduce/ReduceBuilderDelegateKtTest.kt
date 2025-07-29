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

import com.nlab.testkit.faker.genBothify
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class ReduceBuilderDelegateKtTest {
    @Test
    fun `Given scope, When build from delegate with scope, Then transition, effect has a scope`() {
        val scope = genBothify()
        val delegate = ReduceBuilderDelegate(scope)
        delegate.addTransitionNode { dslTransitionScope: TestDslTransitionScope -> dslTransitionScope.current }
        delegate.addSuspendEffectNode { _: TestDslSuspendEffectScope -> }

        val transition = checkNotNull(delegate.buildTransition())
        val effect = checkNotNull(delegate.buildEffect())
        assertThat(transition.scope, equalTo(scope))
        assertThat(effect.scope, equalTo(scope))
    }
}