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
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.nlab.reminder.core.androidx.navigation.compose.EnterTransitionFactory
import com.nlab.reminder.core.androidx.navigation.compose.ExitTransitionFactory
import com.nlab.reminder.core.androidx.navigation.compose.Navigator
import com.nlab.reminder.core.androidx.navigation.compose.toEntries
import com.nlab.reminder.feature.all.navigation.allEntry
import com.nlab.reminder.feature.all.navigation.navigateToAll
import com.nlab.reminder.feature.home.navigation.homeEntry

/**
 * @author Thalys
 */
@Composable
fun PlaneatNavHost(
    appState: PlaneatAppState,
    modifier: Modifier = Modifier
) {
    val navigationState = appState.navigationState
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val entryProvider = entryProvider {
        homeEntry(
            onTodayCategoryClicked = {
                // TODO implement
            },
            onTimetableCategoryClicked = {
                // TODO implement
            },
            onAllCategoryClicked = { navigator.navigateToAll() }
        )
        allEntry(
            enterTransitionTimeInMillis = HOME_TRANSITION_DURATION,
            // The transition between Home and All is decided by the metadata of All, because Navigation 3 uses
            // the metadata of the entry that is pushed (or popped) for both of the entries involved.
            metadata = NavDisplay.transitionSpec { homeToAllTransform() } +
                    NavDisplay.popTransitionSpec { allToHomeTransform() } +
                    NavDisplay.predictivePopTransitionSpec { allToHomeTransform() },
            showAppToast = { appState.showApplicationToast(it) },
            onBackClicked = { navigator.goBack() }
        )
    }
    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        modifier = modifier,
        onBack = { navigator.goBack() },
        transitionSpec = { noneTransform() },
        popTransitionSpec = { noneTransform() },
        predictivePopTransitionSpec = { noneTransform() }
    )
}

private const val HOME_TRANSITION_DURATION = 300

private val fromHomeEnterTransitionFactory: EnterTransitionFactory = {
    slideIntoContainer(
        animationSpec = tween(HOME_TRANSITION_DURATION, easing = EaseIn),
        towards = AnimatedContentTransitionScope.SlideDirection.Start
    )
}

private val homeExitTransitionFactory: ExitTransitionFactory = {
    slideOutOfContainer(
        animationSpec = tween(HOME_TRANSITION_DURATION, easing = EaseOut),
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        targetOffset = { (it * 0.3f).toInt() }
    ) + fadeOut(animationSpec = tween(HOME_TRANSITION_DURATION, easing = LinearEasing), targetAlpha = 0.2f)
}

private val homePopEnterTransitionFactory: EnterTransitionFactory = {
    slideIntoContainer(
        animationSpec = tween(HOME_TRANSITION_DURATION, easing = EaseIn),
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        initialOffset = { (it * 0.3f).toInt() }
    ) + fadeIn(animationSpec = tween(HOME_TRANSITION_DURATION, easing = LinearEasing), initialAlpha = 0.2f)
}

private val fromHomePopExitTransitionFactory: ExitTransitionFactory = {
    slideOutOfContainer(
        animationSpec = tween(HOME_TRANSITION_DURATION, easing = EaseOut),
        towards = AnimatedContentTransitionScope.SlideDirection.End
    )
}

private fun noneTransform(): ContentTransform = EnterTransition.None togetherWith ExitTransition.None

private fun AnimatedContentTransitionScope<Scene<*>>.homeToAllTransform(): ContentTransform =
    (fromHomeEnterTransitionFactory() ?: EnterTransition.None) togetherWith
            (homeExitTransitionFactory() ?: ExitTransition.None)

private fun AnimatedContentTransitionScope<Scene<*>>.allToHomeTransform(): ContentTransform =
    (homePopEnterTransitionFactory() ?: EnterTransition.None) togetherWith
            (fromHomePopExitTransitionFactory() ?: ExitTransition.None)
