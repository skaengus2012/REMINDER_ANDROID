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

package com.nlab.testkit.faker

import kotlin.reflect.KClass

/**
 * @author Doohyun
 */
fun <T> Iterable<T>.shuffleAndGetFirst(
    predicate: (T) -> Boolean = { true }
): T {
    val target = filter(predicate).shuffled()
    check(target.isNotEmpty()) { "Cannot found any element satisfying the predicate" }

    return target.first()
}

fun <T : Any> Iterable<T>.requireSample(): T = shuffled().first()

fun <T : Any> Iterable<T>.requireSampleExcludeTypeOf(
    types: List<KClass<out T>>,
): T = shuffleAndGetFirst { sample ->
    types.all { it.isInstance(sample).not() }
}