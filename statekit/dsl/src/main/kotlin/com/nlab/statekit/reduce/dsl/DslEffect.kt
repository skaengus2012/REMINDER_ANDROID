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

import com.nlab.statekit.reduce.ActionDispatcher

/**
 * @author Thalys
 */
internal sealed interface DslEffect<R : Any, A : Any, S : Any> {
    fun interface NodeEffect<R : Any, A : Any, S : Any> : DslEffect<R, A, S> {
        suspend fun invoke(action: A, current: S, actionDispatcher: ActionDispatcher<R>)
    }

    class CompositeEffect<R : Any, A : Any, S : Any>(
        val effects: List<DslEffect<R, A, S>>
    ) : DslEffect<R, A, S>

    class PredicateScopeEffect<R : Any, A : Any, S : R>(
        val predicate: (UpdateSource<A, S>) -> Boolean,
        val effect: DslEffect<R, A, S>
    ) : DslEffect<R, A, S>

    class TransformSourceScopeEffect<R : Any, A : Any, S : R, T : Any, U : R>(
        val transformSource: (UpdateSource<A, S>) -> UpdateSource<T, U>?,
        val effect: DslEffect<R, T, U>
    ) : DslEffect<R, A, S>
}