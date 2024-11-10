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

package com.nlab.statekit.dsl.reduce

/**
 * @author Doohyun
 */
interface UpdateSource<A : Any, S : Any> {
    val action: A
    val current: S
}

fun <A : Any, S : Any> UpdateSource(
    action: A,
    current: S
): UpdateSource<A, S> = object : UpdateSource<A, S> {
    override val action: A = action
    override val current: S = current
}