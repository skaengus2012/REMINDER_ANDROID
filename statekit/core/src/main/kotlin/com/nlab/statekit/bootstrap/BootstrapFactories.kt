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

package com.nlab.statekit.bootstrap

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * @author Doohyun
 */
fun <A : Any> Bootstrap(
    action: A,
    isPendingBootUntilSubscribed: Boolean = true
): Bootstrap<A> = FlowBootstrap(
    actionStream = flowOf(action),
    started = if (isPendingBootUntilSubscribed) DeliveryStarted.Lazily else DeliveryStarted.Eagerly
)

@Suppress("FunctionName")
fun <A : Any> EmptyBootstrap(): Bootstrap<A> = EmptyBootstrap

fun <A : Any> Flow<A>.collectAsBootstrap(started: DeliveryStarted): Bootstrap<A> =
    FlowBootstrap(actionStream = this, started = started)

fun <A : Any> composeBootstrap(
    first: Bootstrap<A>,
    second: Bootstrap<A>,
    vararg etc: Bootstrap<A>
): Bootstrap<A> = CompositeBootstrap(
    head = first,
    tails = buildList {
        add(second)
        if (etc.isNotEmpty()) addAll(etc)
    }
)

fun <A : Any> composeBootstrap(bootstraps: List<Bootstrap<A>>): Bootstrap<A> {
    if (bootstraps.isEmpty()) return EmptyBootstrap()
    if (bootstraps.size == 1) return bootstraps.first()
    return CompositeBootstrap(head = bootstraps.first(), tails = bootstraps.drop(1))
}

operator fun <A : Any> Bootstrap<A>.plus(other: Bootstrap<A>): Bootstrap<A> =
    composeBootstrap(first = this, second = other)