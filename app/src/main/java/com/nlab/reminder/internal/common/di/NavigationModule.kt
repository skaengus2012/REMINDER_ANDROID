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

package com.nlab.reminder.internal.common.di

import com.nlab.reminder.R
import com.nlab.reminder.core.effect.message.navigation.android.NavigationMediator
import com.nlab.reminder.core.effect.message.navigation.android.util.NavigationMediator
import com.nlab.reminder.domain.common.effect.message.navigation.*
import com.nlab.reminder.domain.common.effect.message.navigation.android.runner.*
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
class NavigationModule {
    @Reusable
    @Provides
    fun provideFragmentNavigateUseCase(
        navigateTodayEnd: TodayEndNavigationEffectRunner,
        navigateTimeTable: TimetableEndNavigationEffectRunner,
        navigateTagEnd: TagEndNavigationEffectRunner
    ): NavigationMediator = NavigationMediator { navController, message ->
        when (message) {
            is TodayEndNavigationMessage -> navigateTodayEnd(navController)
            is TimetableEndNavigationMessage -> navigateTimeTable(navController)
            is AllEndNavigationMessage -> navController.navigate(R.id.action_global_allScheduleFragment)
            is TagEndNavigationMessage -> navigateTagEnd(navController, message.tag)
            else -> Unit
        }
    }

    @Provides
    fun provideTodayEndNavigate() = TodayEndNavigationEffectRunner()

    @Provides
    fun provideTimetableEndNavigate() = TimetableEndNavigationEffectRunner()

    @Provides
    fun provideTagEndNavigate() = TagEndNavigationEffectRunner()
}