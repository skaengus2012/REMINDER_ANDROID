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

package com.nlab.reminder.feature.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.nlab.reminder.core.androidx.navigation.compose.EnterTransitionFactory
import com.nlab.reminder.core.androidx.navigation.compose.ExitTransitionFactory
import com.nlab.reminder.feature.all.ui.AllScreen
import kotlinx.serialization.Serializable

/**
 * @author Doohyun
 */
@Serializable
internal data object AllRoute

fun NavController.navigateToAll(navOptions: NavOptionsBuilder.() -> Unit = {}) =
    navigate(route = AllRoute, builder = navOptions)

fun NavGraphBuilder.allScreen(
    onBackClicked: () -> Unit,
    provideEnterTransition: EnterTransitionFactory? = null,
    providePopExitTransition: ExitTransitionFactory? = null
) {
    composable<AllRoute>(
        enterTransition = provideEnterTransition,
        popExitTransition = providePopExitTransition
    ) {
        AllScreen(onBackClicked = onBackClicked)
    }
}