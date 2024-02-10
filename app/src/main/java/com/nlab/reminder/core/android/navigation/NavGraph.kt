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

import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.core.kotlin.util.catching
import com.nlab.reminder.core.kotlin.util.getOrThrow
import kotlin.reflect.KClass

/**
 * @author Doohyun
 */
class NavGraph<T> internal constructor(
    private val mapper: Map<KClass<out Screen>, NavNode<T>>
) {
    fun navigateAsResult(navHandle: T, screen: Screen): Result<Unit> = catching {
        mapper[screen::class]?.visit(navHandle, screen) ?: throw IllegalStateException(
            "Screen not found, Please inject navigation strategy [$screen]"
        )
    }

    fun navigate(navHandle: T, screen: Screen) {
        navigateAsResult(navHandle, screen).getOrThrow()
    }
}

fun <T> NavGraph(block: NavGraphBuilder<T>.() -> Unit): NavGraph<T> = NavGraph(
    NavGraphBuilder<T>()
        .apply(block)
        .build()
)