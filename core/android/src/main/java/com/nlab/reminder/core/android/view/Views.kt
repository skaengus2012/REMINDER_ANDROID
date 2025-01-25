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

package com.nlab.reminder.core.android.view

import android.view.MotionEvent
import android.view.View
import com.nlab.reminder.core.kotlinx.coroutine.flow.throttleFirst
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * @author thalys
 */

fun View.setVisible(isVisible: Boolean, goneIfNotVisible: Boolean = true) {
    visibility = when {
        isVisible -> View.VISIBLE
        goneIfNotVisible -> View.GONE
        else -> View.INVISIBLE
    }
}

val View.isVisible: Boolean
    get() = visibility == View.VISIBLE

fun View.bindSelected(selected: Boolean): Boolean {
    if (this.isSelected == selected) return false
    this.isSelected = selected
    return true
}

fun View.clearFocusIfNeeded(): Boolean {
    if (isFocused.not()) return false
    clearFocus()
    return true
}

suspend fun View.awaitPost() = suspendCancellableCoroutine { continuation ->
    val runnable = Runnable { continuation.resume(Unit) }
    post(runnable)

    continuation.invokeOnCancellation { removeCallbacks(runnable) }
}

fun View.touches(): Flow<MotionEvent> = callbackFlow {
    setOnTouchListener { v, event ->
        trySend(event)
        v.performClick()
    }

    awaitClose { setOnTouchListener(null) }
}

fun View.clicks(): Flow<View> = callbackFlow {
    setOnClickListener { trySend(it) }
    awaitClose { setOnClickListener(null) }
}

fun View.throttleClicks(windowDuration: Long = 500): Flow<View> = clicks().throttleFirst(windowDuration)

fun View.focusChanges(): Flow<Boolean> = callbackFlow {
    val listener = View.OnFocusChangeListener { _, hasFocus ->
        trySend(hasFocus)
    }
    onFocusChangeListener = listener
    awaitClose { onFocusChangeListener = null }
}

fun View.focusState(
    scope: CoroutineScope,
    started: SharingStarted
): Flow<Boolean> = focusChanges().stateIn(scope, started, hasFocus())