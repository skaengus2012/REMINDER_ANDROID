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
import androidx.navigation3.scene.Scene

internal const val HOME_TRANSITION_DURATION = 300

internal fun noneTransform(): ContentTransform = EnterTransition.None togetherWith ExitTransition.None

internal fun AnimatedContentTransitionScope<Scene<*>>.homeToAllTransform(): ContentTransform =
    slideIntoContainer(
        animationSpec = tween(HOME_TRANSITION_DURATION, easing = EaseIn),
        towards = AnimatedContentTransitionScope.SlideDirection.Start
    ) togetherWith (
        slideOutOfContainer(
            animationSpec = tween(HOME_TRANSITION_DURATION, easing = EaseOut),
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            targetOffset = { (it * 0.3f).toInt() }
        ) + fadeOut(animationSpec = tween(HOME_TRANSITION_DURATION, easing = LinearEasing), targetAlpha = 0.2f)
    )

internal fun AnimatedContentTransitionScope<Scene<*>>.allToHomeTransform(): ContentTransform =
    (slideIntoContainer(
        animationSpec = tween(HOME_TRANSITION_DURATION, easing = EaseIn),
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        initialOffset = { (it * 0.3f).toInt() }
    ) + fadeIn(animationSpec = tween(HOME_TRANSITION_DURATION, easing = LinearEasing), initialAlpha = 0.2f)) togetherWith
            slideOutOfContainer(
                animationSpec = tween(HOME_TRANSITION_DURATION, easing = EaseOut),
                towards = AnimatedContentTransitionScope.SlideDirection.End
            )
