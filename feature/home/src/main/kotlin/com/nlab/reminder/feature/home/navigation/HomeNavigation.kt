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

package com.nlab.reminder.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.nlab.reminder.core.androidx.navigation.compose.EnterTransitionFactory
import com.nlab.reminder.core.androidx.navigation.compose.ExitTransitionFactory
import com.nlab.reminder.feature.home.ui.HomeScreen
import kotlinx.serialization.Serializable

/**
 * @author Thalys
 */
@Serializable
internal data object HomeRoute

@Serializable
data object HomeEntryPointRoute

fun NavGraphBuilder.homeEntryPoint(builder: NavGraphBuilder.() -> Unit) {
    navigation<HomeEntryPointRoute>(startDestination = HomeRoute, builder = builder)
}

fun NavGraphBuilder.homeScreen(
    onTodayCategoryClicked: () -> Unit,
    onTimetableCategoryClicked: () -> Unit,
    onAllCategoryClicked: () -> Unit,
    provideExitTransition: ExitTransitionFactory? = null,
    providePopEnterTransition: EnterTransitionFactory? = null
) {
    composable<HomeRoute>(
        exitTransition = provideExitTransition,
        popEnterTransition = providePopEnterTransition
    ) {
        HomeScreen(
            onTodayCategoryClicked = onTodayCategoryClicked,
            onTimetableCategoryClicked = onTimetableCategoryClicked,
            onAllCategoryClicked = onAllCategoryClicked
        )
    }
}

