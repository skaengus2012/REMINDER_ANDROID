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

package com.nlab.reminder.core.androix.recyclerview

import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * @author thalys
 */
fun RecyclerView.scrollState(): Flow<Int> = callbackFlow {
    val listener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            trySend(newState)
        }
    }
    addOnScrollListener(listener)
    awaitClose { removeOnScrollListener(listener) }
}

fun RecyclerView.verticalScrollRange(): Flow<Int> = callbackFlow {
    val conflateFlow = MutableStateFlow(computeVerticalScrollRange())
    conflateFlow
        .onEach { send(it) }
        .launchIn(scope = this)
    val listener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            conflateFlow.value = recyclerView.computeVerticalScrollRange()
        }
    }
    addOnScrollListener(listener)
    awaitClose { removeOnScrollListener(listener) }
}

data object ScrollEvent
fun RecyclerView.scrollEvent(): Flow<ScrollEvent> = callbackFlow {
    val listener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            trySend(ScrollEvent)
        }
    }
    addOnScrollListener(listener)
    awaitClose { removeOnScrollListener(listener) }
}