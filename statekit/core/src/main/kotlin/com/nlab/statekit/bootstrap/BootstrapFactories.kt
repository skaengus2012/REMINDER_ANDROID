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

/**
 * @author Doohyun
 */
@Suppress("FunctionName")
fun <A : Any> EmptyBootstrap(): Bootstrap<A> = EmptyBootstrap

fun <A : Any> Bootstrap(
    action: A,
    isPendingBootUntilSubscribed: Boolean = false
): Bootstrap<A> = SingleBootstrap(action, isPendingBootUntilSubscribed)

fun <A : Any> Bootstrap(
    bootstraps: List<Bootstrap<A>>,
): Bootstrap<A> = when (bootstraps.size) {
    0 -> EmptyBootstrap
    1 -> bootstraps.first()
    else -> MergeBootstrap(bootstraps)
}

fun <A : Any> Bootstrap(
    first: Bootstrap<A>,
    second: Bootstrap<A>,
    vararg etc: Bootstrap<A>
): Bootstrap<A> = MergeBootstrap(buildList {
    add(first)
    add(second)
    addAll(etc)
})

operator fun <A : Any> Bootstrap<A>.plus(other: Bootstrap<A>): Bootstrap<A> {
    val newBootstraps = when {
        this is MergeBootstrap && other is MergeBootstrap -> bootstraps + other.bootstraps
        this is MergeBootstrap -> bootstraps + other
        other is MergeBootstrap -> buildList { add(this@plus); addAll(other.bootstraps) }
        else -> listOf(this, other)
    }
    return MergeBootstrap(newBootstraps)
}
