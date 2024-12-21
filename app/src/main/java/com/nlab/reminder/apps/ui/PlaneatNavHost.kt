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

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.nlab.reminder.domain.feature.home.navigation.HomeBaseRoute
import com.nlab.reminder.domain.feature.home.navigation.homeScreen
import com.nlab.reminder.domain.feature.schedule.all.navigation.allScheduleScreen
import com.nlab.reminder.domain.feature.schedule.all.navigation.navigateToAllSchedule

/**
 * @author Thalys
 */
@Composable
fun PlaneatNavHost(
    appState: PlaneatAppState,
    modifier: Modifier = Modifier
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = HomeBaseRoute,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        homeScreen(
            onAllScheduleClicked = {
                navController.navigateToAllSchedule()
            },
            allScheduleDestination = {
                allScheduleScreen(
                    onBackClicked = { navController.popBackStack() }
                )
            }
        )
    }
}