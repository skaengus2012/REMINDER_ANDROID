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

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
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
    onBackClicked: () -> Unit
) {
    composable<AllScheduleRoute>(
        enterTransition = {
           slideIntoContainer(
                animationSpec = tween(150, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        },
        exitTransition = {
            slideOutOfContainer(
                animationSpec = tween(150, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.End
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                animationSpec = tween(150, easing = EaseIn),
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                initialOffset = { fullWidth -> (fullWidth * 0.3f).toInt() }
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                animationSpec = tween(150, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )
        }
    ) {
        AllScheduleScreen(
            onBackClicked = onBackClicked
        )
    }
}
