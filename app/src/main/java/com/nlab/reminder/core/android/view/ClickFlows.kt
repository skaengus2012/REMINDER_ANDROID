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

package com.nlab.reminder.core.android.view

import android.view.View
import com.nlab.reminder.core.kotlin.flow.throttleFirst
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * @author Doohyun
 */
fun View.clicks(): Flow<View> = callbackFlow {
    setOnClickListener { trySend(it) }
    awaitClose { setOnClickListener(null) }
}

fun View.longClicks(isEventConsumed: Boolean = false): Flow<View> = callbackFlow {
    setOnLongClickListener { trySend(it); isEventConsumed }
    awaitClose { setOnLongClickListener(null) }
}

fun View.throttleClicks(windowDuration: Long = 100): Flow<View> = clicks().throttleFirst(windowDuration)

fun View.throttleLongClicks(
    windowDuration: Long = 100,
    isEventConsumed: Boolean = false
): Flow<View> = longClicks(isEventConsumed).throttleFirst(windowDuration)