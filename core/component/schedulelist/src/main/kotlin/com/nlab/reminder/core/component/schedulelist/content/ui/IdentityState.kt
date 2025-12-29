/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.schedulelist.content.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map

internal typealias IdentityFlow<T> = Flow<Identity<T>>
internal typealias IdentityStateFlow<T> = StateFlow<Identity<T>>
internal typealias MutableIdentityStateFlow<T> = MutableStateFlow<Identity<T>>

/**
 * @author Thalys
 */
internal sealed class Identity<out T> {
    data object Absent : Identity<Nothing>()
    class Exist<T>(val value: T) : Identity<T>()
}

internal fun <T> MutableIdentityStateFlow(): MutableIdentityStateFlow<T> =
    MutableStateFlow(Identity.Absent)

internal fun <T> MutableIdentityStateFlow<T>.update(newValue: T) {
    value = Identity.Exist(newValue)
}

internal fun <T> IdentityFlow<T>.unwrap(): Flow<T> = filterIsInstance<Identity.Exist<T>>().map { it.value }