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

package com.nlab.reminder.core.androidx.navigation

import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Owns the [backStack] and handles navigation events on it with [navigate] and [goBack].
 * [backStack] is exposed for rendering, so only hand over a [Navigator] to features, and move through [navigate] and
 * [goBack] to keep the rules (no duplicated key, never empty).
 * Create the back stack with `rememberNavBackStack` and keep this with `remember`, so that the back stack is restored
 * across configuration changes and process death.
 *
 * @author Thalys
 */
@Stable
class Navigator(val backStack: NavBackStack<NavKey>) {
    /**
     * Moves [key] to the top of the back stack. If the key is already in the stack, it is moved instead of duplicated.
     * This applies to the start key too, so navigating to it does not clear the stack.
     */
    fun navigate(key: NavKey) {
        backStack.remove(key)
        backStack.add(key)
    }

    /**
     * Pops the top key. The last remaining key is kept, so back events on the start key are ignored safely.
     *
     * @return the popped key, or `null` if it was ignored because only one key remained.
     */
    fun goBack(): NavKey? = if (backStack.size > 1) backStack.removeLastOrNull() else null
}
