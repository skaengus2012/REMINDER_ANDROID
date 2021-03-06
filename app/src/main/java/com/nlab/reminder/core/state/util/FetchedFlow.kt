/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.core.state.util

import com.nlab.reminder.core.state.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

/**
 * @author Doohyun
 */
fun <S : State> StateFlow<S>.fetchedFlow(
    scope: CoroutineScope,
    onFetch: () -> Unit
): StateFlow<S> = onStart(onFetchToOnStartConverter(onFetch)).stateIn(scope, SharingStarted.Lazily, value)

// Jacoco could not measure coverage in onStart suspend function..
private fun <T> onFetchToOnStartConverter(onFetch: () -> Unit): FlowCollector<T>.() -> Unit = { onFetch() }