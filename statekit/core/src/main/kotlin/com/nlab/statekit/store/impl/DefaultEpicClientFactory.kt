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

import com.nlab.statekit.middleware.epic.EpicClient
import com.nlab.statekit.middleware.epic.SubscriptionStrategy
import com.nlab.statekit.middleware.epic.infra.WhileStateUsedEpicClient
import com.nlab.statekit.store.EpicClientFactory
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @author Doohyun
 */
internal class DefaultEpicClientFactory(
    private val baseState: MutableStateFlow<*>
) : EpicClientFactory() {
    override fun onCreate(subscriptionStrategy: SubscriptionStrategy): EpicClient = when (subscriptionStrategy) {
        SubscriptionStrategy.WhileStateUsed -> {
            WhileStateUsedEpicClient(baseState)
        }
    }
}