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

package com.nlab.reminder.domain.feature.schedule.all.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.nlab.reminder.apps.ui.EnterTransitionFactory
import com.nlab.reminder.apps.ui.ExitTransitionFactory
import com.nlab.reminder.domain.feature.schedule.all.ui.AllScheduleScreen
import kotlinx.serialization.Serializable

/**
 * @author Thalys
 */
@Serializable
internal data object AllScheduleRoute

fun NavController.navigateToAllSchedule(navOptions: NavOptionsBuilder.() -> Unit = {}) =
    navigate(route = AllScheduleRoute, builder = navOptions)

fun NavGraphBuilder.allScheduleScreen(
    onBackClicked: () -> Unit,
    provideEnterTransition: EnterTransitionFactory? = null,
    providePopExitTransition: ExitTransitionFactory? = null
) {
    composable<AllScheduleRoute>(
        enterTransition = provideEnterTransition,
        popExitTransition = providePopExitTransition
    ) {
        AllScheduleScreen(
            onBackClicked = onBackClicked
        )
    }
}