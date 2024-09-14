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

package com.nlab.statekit.reduce

/**
 * @author Thalys
 */
fun <A : Any, S : Any> Reduce(
    transitionTo: (action: A, current: S) -> S,
    launchEffect: suspend (action: A, current: S, actionDispatcher: ActionDispatcher<A>) -> Unit
): Reduce<A, S> = DefaultReduce(
    onTransition = transitionTo,
    onLaunchEffect = launchEffect
)

fun <A : Any, S : Any> emptyReduce(): Reduce<A, S> = EmptyReduce()