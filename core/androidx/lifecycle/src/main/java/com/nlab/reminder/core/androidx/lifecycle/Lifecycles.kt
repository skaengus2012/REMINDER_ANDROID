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

package com.nlab.reminder.core.androidx.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter

/**
 * @author thalys
 */
fun Lifecycle.event(): Flow<Lifecycle.Event> = callbackFlow {
    val listener = LifecycleEventObserver { _, event -> trySend(event) }
    addObserver(listener)
    awaitClose { removeObserver(listener) }
}

fun Flow<Lifecycle.Event>.filterLifecycleEvent(lifecycleEvent: Lifecycle.Event): Flow<Lifecycle.Event> =
    filter { event -> event == lifecycleEvent }