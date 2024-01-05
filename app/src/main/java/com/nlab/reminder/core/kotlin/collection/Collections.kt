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

package com.nlab.reminder.core.kotlin.collection

import kotlin.collections.minOf as kotlinMinOf
import kotlin.collections.maxOf as kotlinMaxOf
import kotlin.collections.filter as kotlinFilter
import kotlin.collections.associateWith as kotlinAssociateWith

/**
 * @author thalys
 */
fun <T, R : Comparable<R>> Iterable<T>.minOf(selector: (T) -> R): R {
    return kotlinMinOf(selector)
}

fun <T, R : Comparable<R>> Iterable<T>.maxOf(selector: (T) -> R): R {
    return kotlinMaxOf(selector)
}

fun <T> Iterable<T>.filter(predicate: (T) -> Boolean): List<T> {
    return kotlinFilter(predicate)
}

fun <K, V> Iterable<K>.associateWith(valueSelector: (K) -> V): Map<K, V> {
    return kotlinAssociateWith(valueSelector)
}
