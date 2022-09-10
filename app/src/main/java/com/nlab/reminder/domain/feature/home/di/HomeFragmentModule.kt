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

import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.nlab.reminder.core.android.navigation.NavigationController
import com.nlab.reminder.core.android.navigation.util.NavigationController
import com.nlab.reminder.core.android.navigation.util.NavigationTable
import com.nlab.reminder.core.android.navigation.util.Navigator
import com.nlab.reminder.domain.feature.home.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped

/**
 * @author Doohyun
 */
@Module
@InstallIn(FragmentComponent::class)
class HomeFragmentModule {
    @HomeScope
    @FragmentScoped
    @Provides
    fun provideNavigationController(
        fragment: Fragment,
        navigationTable: NavigationTable<NavController>,
        @HomeScope homeNavigationTable: NavigationTable<NavController>,
    ): NavigationController = NavigationController(
        Navigator(listOf(homeNavigationTable, navigationTable), getHandler = { fragment.findNavController() })
    )
}