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
package com.nlab.statekit.reduce.dsl

/**
 * @author Doohyun
 */
internal typealias RootReduceBuilder<A, S> = ScopeReduceBuilder<A, S, A, S>

internal fun <A : Any, S : Any> RootReduceBuilder<A, S>.build() = DslReduce(
    transition = buildTransition(),
    effect = buildEffect()
)