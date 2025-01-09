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

import android.view.View
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart

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

fun View.bindSelected(selected: Boolean): Boolean {
    if (this.isSelected == selected) return false
    this.isSelected = selected
    return true
}

fun View.focusChanges(): Flow<Boolean> = callbackFlow {
    val listener = View.OnFocusChangeListener { _, hasFocus ->
        trySend(hasFocus)
    }
    onFocusChangeListener = listener
    awaitClose { onFocusChangeListener = null }
}

fun View.focusState(): Flow<Boolean> = focusChanges().onStart { emit(hasFocus()) }