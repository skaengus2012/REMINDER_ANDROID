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

package com.nlab.reminder.apps.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.nlab.reminder.core.androidx.navigation.compose.Navigator
import com.nlab.reminder.core.component.usermessage.eventbus.ui.UserMessageAggregateHandler
import com.nlab.reminder.feature.all.impl.ui.allEntry
import com.nlab.reminder.feature.home.impl.ui.homeEntry

/**
 * @author Thalys
 */
@Composable
fun PlaneatApp(appState: PlaneatAppState) {
    val navigator = remember(appState.backStack) { Navigator(appState.backStack) }
    val entryProvider = entryProvider {
        homeEntry(navigator = navigator)
        allEntry(
            navigator = navigator,
            enterTransitionTimeInMillis = HOME_TRANSITION_DURATION,
            // The transition between Home and All is decided by the metadata of All, because Navigation 3 uses
            // the metadata of the entry that is pushed (or popped) for both of the entries involved.
            metadata = NavDisplay.transitionSpec { homeToAllTransform() } +
                    NavDisplay.popTransitionSpec { allToHomeTransform() } +
                    NavDisplay.predictivePopTransitionSpec { allToHomeTransform() },
            showAppToast = { appState.showApplicationToast(it) }
        )
    }
    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = navigator.backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider,
        onBack = { navigator.goBack() },
        transitionSpec = { noneTransform() },
        popTransitionSpec = { noneTransform() },
        predictivePopTransitionSpec = { noneTransform() }
    )
    UserMessageAggregateHandler(
        showUserMessage = { message, _ ->
            // TODO show user message with priority
            appState.showApplicationToast(message)
        }
    )
}