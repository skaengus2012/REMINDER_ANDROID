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

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.nlab.reminder.core.androidx.navigation.compose.EnterTransitionFactory
import com.nlab.reminder.core.androidx.navigation.compose.ExitTransitionFactory
import com.nlab.reminder.feature.home.navigation.HomeEntryPointRoute
import com.nlab.reminder.feature.home.navigation.homeEntryPoint
import com.nlab.reminder.feature.home.navigation.homeScreen
import com.nlab.reminder.feature.all.navigation.allScreen
import com.nlab.reminder.feature.all.navigation.navigateToAll

/**
 * @author Thalys
 */
private const val HOME_TRANSITION_DURATION = 300

@Composable
fun PlaneatNavHost(
    appState: PlaneatAppState,
    modifier: Modifier = Modifier
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = HomeEntryPointRoute,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        homeEntryPoint {
            val enterTransitionFactoryFromHome = createFromHomeEnterTransitionFactory()
            val popExitTransitionFactoryFromHome = createFromHomePopExitTransitionFactory()
            homeScreen(
                provideExitTransition = createHomeExitTransition(),
                providePopEnterTransition = createHomePopEnterTransitionFactory(),
                onTodayCategoryClicked = {
                    // TODO implement
                },
                onTimetableCategoryClicked = {
                    // TODO implement
                },
                onAllCategoryClicked = { navController.navigateToAll() }
            )
            allScreen(
                provideEnterTransition = enterTransitionFactoryFromHome,
                providePopExitTransition = popExitTransitionFactoryFromHome,
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}

private fun createFromHomeEnterTransitionFactory(): EnterTransitionFactory = {
    slideIntoContainer(
        animationSpec = tween(HOME_TRANSITION_DURATION, easing = EaseIn),
        towards = AnimatedContentTransitionScope.SlideDirection.Start
    )
}

private fun createHomeExitTransition(): ExitTransitionFactory = {
    slideOutOfContainer(
        animationSpec = tween(HOME_TRANSITION_DURATION, easing = EaseOut),
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        targetOffset = { (it * 0.3f).toInt() }
    ) + fadeOut(animationSpec = tween(HOME_TRANSITION_DURATION, easing = LinearEasing), targetAlpha = 0.2f)
}

private fun createHomePopEnterTransitionFactory(): EnterTransitionFactory = {
    slideIntoContainer(
        animationSpec = tween(HOME_TRANSITION_DURATION, easing = EaseIn),
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        initialOffset = { (it * 0.3f).toInt() }
    ) + fadeIn(animationSpec = tween(HOME_TRANSITION_DURATION, easing = LinearEasing), initialAlpha = 0.2f)
}

private fun createFromHomePopExitTransitionFactory(): ExitTransitionFactory = {
    slideOutOfContainer(
        animationSpec = tween(HOME_TRANSITION_DURATION, easing = EaseOut),
        towards = AnimatedContentTransitionScope.SlideDirection.End
    )
}
