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

package com.nlab.reminder.feature.all.impl.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.nlab.reminder.core.androidx.navigation.compose.Navigator
import com.nlab.reminder.feature.all.api.navigation.AllNavKey

/**
 * @param metadata [androidx.navigation3.runtime.NavEntry] metadata, such as `NavDisplay.transitionSpec`.
 *
 * @author Doohyun
 */
fun EntryProviderScope<NavKey>.allEntry(
    navigator: Navigator,
    enterTransitionTimeInMillis: Int,
    metadata: Map<String, Any> = emptyMap(),
    showAppToast: (String) -> Unit,
) {
    entry<AllNavKey>(metadata = metadata) {
        AllScreen(
            enterTransitionTimeInMillis = enterTransitionTimeInMillis.toLong(),
            onBackClicked = { navigator.goBack() },
            showAppToast = showAppToast
        )
    }
}
