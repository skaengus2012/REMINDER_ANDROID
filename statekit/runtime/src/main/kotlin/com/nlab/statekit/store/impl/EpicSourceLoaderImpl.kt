/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.statekit.store.impl

import com.nlab.statekit.Action
import com.nlab.statekit.State
import com.nlab.statekit.middleware.enhancer.ActionDispatcher
import com.nlab.statekit.middleware.epic.EpicClient
import com.nlab.statekit.middleware.epic.EpicSource
import com.nlab.statekit.middleware.epic.SubscriptionStrategy
import com.nlab.statekit.middleware.epic.util.EpicClientFactory
import com.nlab.statekit.store.EpicSourceLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @author thalys
 */
internal class EpicSourceLoaderImpl : EpicSourceLoader {
    override fun <A : Action, S : State> load(
        coroutineScope: CoroutineScope,
        epicSources: List<EpicSource<A>>,
        actionDispatcher: ActionDispatcher<A>,
        stateFlow: MutableStateFlow<S>
    ) {
        if (epicSources.isNotEmpty()) {
            var whileStateUsedEpicClient: EpicClient? = null
            epicSources.forEach { epicSource ->
                val epicClient = when (epicSource.subscriptionStrategy) {
                    SubscriptionStrategy.WhileStateUsed -> {
                        whileStateUsedEpicClient
                            ?: EpicClientFactory.getWhileStateUsed(stateFlow).also { whileStateUsedEpicClient = it }
                    }
                }

                epicClient.fetch(coroutineScope, epicSource.stream, actionDispatcher)
            }
        }
    }
}