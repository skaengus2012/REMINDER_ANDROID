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

package com.nlab.reminder.core.androidx.navigation.compose

import androidx.navigation3.runtime.NavKey

/**
 * @author Thalys
 */
class Navigator(private val state: NavigationState) {
    fun navigate(key: NavKey) {
        val backStack = state.backStack
        if (key == state.startKey) {
            backStack.clear()
            backStack.add(key)
        } else {
            backStack.remove(key)
            backStack.add(key)
        }
    }

    fun goBack() {
        val backStack = state.backStack
        check(backStack.size > 1) { "You cannot go back from the start key: ${state.startKey}" }
        backStack.removeLastOrNull()
    }
}
