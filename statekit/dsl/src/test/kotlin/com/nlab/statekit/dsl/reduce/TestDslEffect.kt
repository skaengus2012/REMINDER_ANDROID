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
import com.nlab.statekit.reduce.Reduce
import com.nlab.statekit.test.reduce.ReduceTestBuilder
import com.nlab.statekit.test.reduce.test

/**
 * @author Doohyun
 */
typealias TestDslEffectScope = DslEffectScope<TestAction, TestAction>
typealias TestDslSuspendEffectScope = DslSuspendEffectScope<TestAction, TestAction, TestState>
internal typealias TestDslEffectNode = DslEffect.Node<TestAction, TestState>
internal typealias TestDslEffectSuspendNode = DslEffect.SuspendNode<TestAction, TestAction, TestState>
internal typealias TestDslEffectPredicateScope = DslEffect.PredicateScope<TestAction, TestState>
internal typealias TestDslEffectTransformScope<T, U> = DslEffect.TransformSourceScope<TestAction, TestState, T, U>

internal fun DslEffect.toReduceTestBuilder(): ReduceTestBuilder<TestAction, TestState> =
    Reduce<TestAction, TestState>(effect = effectOf(dslEffect = this))
        .test()