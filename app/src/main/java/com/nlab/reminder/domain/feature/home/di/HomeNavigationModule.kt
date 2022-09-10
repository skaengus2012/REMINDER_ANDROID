/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.domain.feature.home.di

import androidx.navigation.NavController
import com.nlab.reminder.core.android.navigation.navcontroller.condition
import com.nlab.reminder.core.android.navigation.util.NavigationTable
import com.nlab.reminder.domain.feature.home.*
import com.nlab.reminder.domain.feature.home.view.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

/**
 * @author Doohyun
 */
@Module
@InstallIn(FragmentComponent::class)
class HomeNavigationModule {
    @HomeScope
    @Provides
    fun provideNavControllerTable(): NavigationTable<NavController> = NavigationTable {
        condition<HomeTagConfigNavigation> { (navController, navigation) ->
            HomeFragmentDirections
                .actionHomeFragmentToHomeConfigDialogFragment(navigation.requestKey, navigation.tag)
                .run(navController::navigate)
        }
        condition<HomeTagRenameNavigation> { (navController, navigation) ->
            HomeFragmentDirections
                .actionHomeFragmentToHomeTagRenameDialogFragment(
                    navigation.requestKey,
                    navigation.tag,
                    navigation.usageCount
                )
                .run(navController::navigate)
        }
        condition<HomeTagDeleteNavigation> { (navController, navigation) ->
            HomeFragmentDirections
                .actionHomeFragmentToHomeTagDeleteDialogFragment(
                    navigation.requestKey,
                    navigation.tag,
                    navigation.usageCount
                )
                .run(navController::navigate)
        }
    }
}