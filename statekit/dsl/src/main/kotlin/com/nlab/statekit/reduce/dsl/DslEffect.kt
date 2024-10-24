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
 * @author Thalys
 */
internal sealed interface DslEffect {
    val scope: Any

    class NodeEffect<out R : Any, out A : Any, out S : Any>(
        override val scope: Any,
        val invoke: suspend (DslEffectScope<@UnsafeVariance A, @UnsafeVariance S, @UnsafeVariance R>) -> Unit
    ) : DslEffect

    class CompositeEffect(
        override val scope: Any,
        val effects: List<DslEffect>
    ) : DslEffect

    class PredicateScopeEffect<out A : Any, out S : Any>(
        override val scope: Any,
        val isMatch: (UnsafeUpdateSource<A, S>) -> Boolean,
        val effect: DslEffect
    ) : DslEffect

    class TransformSourceScopeEffect<out A : Any, out S : Any, out T : Any, out U : Any>(
        override val scope: Any,
        val subScope: Any,
        val transformSource: (UnsafeUpdateSource<A, S>) -> UnsafeUpdateSource<T, U>?,
        val effect: DslEffect
    ) : DslEffect
}