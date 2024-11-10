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

import com.nlab.statekit.reduce.ActionDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

/**
 * @author Doohyun
 */
internal class CompositeBootstrap<out A : Any>(
    private val head: Bootstrap<A>,
    private val tails: Collection<Bootstrap<A>>,
) : Bootstrap<A>() {
    override fun onFetched(
        coroutineScope: CoroutineScope,
        actionDispatcher: ActionDispatcher<A>,
        stateSubscriptionCount: StateFlow<Int>
    ): Set<Job> {
        val ret = mutableSetOf<Job>()
        fetchInternal(
            node = head,
            coroutineScope,
            actionDispatcher,
            stateSubscriptionCount,
            ArrayDeque(tails),
            ret
        )
        return ret
    }

    private tailrec fun fetchInternal(
        node: Bootstrap<A>?,
        coroutineScope: CoroutineScope,
        actionDispatcher: ActionDispatcher<A>,
        stateSubscriptionCount: StateFlow<Int>,
        acc: ArrayDeque<Bootstrap<A>>,
        jobs: MutableSet<Job>
    ) {
        if (node == null) return
        val executableNode = if (node is CompositeBootstrap) {
            acc += node.tails
            node.head
        } else {
            node
        }
        jobs += executableNode.fetch(
            coroutineScope,
            actionDispatcher,
            stateSubscriptionCount
        )
        fetchInternal(
            acc.removeFirstOrNull(),
            coroutineScope,
            actionDispatcher,
            stateSubscriptionCount,
            acc,
            jobs
        )
    }
}