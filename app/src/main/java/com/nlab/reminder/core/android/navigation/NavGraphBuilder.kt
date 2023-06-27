/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.core.android.navigation

import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * @author Doohyun
 */
@NavGraphDsl
class NavGraphBuilder<T> internal constructor() {
    private val table: MutableMap<KClass<out Screen>, NavNode<T>> = mutableMapOf()

    fun <S : Screen> node(screenClazz: KClass<S>, block: NavGraphScope<T>.(S) -> Unit) {
        table[screenClazz] = object : NavNode<T>() {
            override fun visit(navHandle: T, screen: Screen) {
                block(NavGraphScope(navHandle), screenClazz.cast(screen))
            }
        }
    }

    inline fun <reified S : Screen> node(noinline block: NavGraphScope<T>.(S) -> Unit) {
        node(S::class, block)
    }

    fun build(): Map<KClass<out Screen>, NavNode<T>> = table.toMap()
}