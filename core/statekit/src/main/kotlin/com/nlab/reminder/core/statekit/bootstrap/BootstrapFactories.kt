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

package com.nlab.reminder.core.statekit.bootstrap

import com.nlab.statekit.bootstrap.Bootstrap
import com.nlab.statekit.bootstrap.DeliveryStarted
import com.nlab.statekit.bootstrap.collectAsBootstrap as collectAsBootstrapOrigin
import kotlinx.coroutines.flow.Flow

/**
 * @author Doohyun
 */

/**
 * Create a Bootstrap that is valid only when there are subscribers of the state.
 * @param started Inject a collect strategy. If you don't put it in, Subscriptions should be canceled with a 5-second grace period.
 */
fun <A : Any> Flow<A>.collectAsBootstrap(
    started: DeliveryStarted = DeliveryStarted.WhileSubscribed(stopTimeoutMillis = 5_000)
): Bootstrap<A> = collectAsBootstrapOrigin(started)