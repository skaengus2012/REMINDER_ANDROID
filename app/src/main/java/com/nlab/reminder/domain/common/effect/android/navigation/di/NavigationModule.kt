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

package com.nlab.reminder.domain.common.effect.android.navigation.di

import com.nlab.reminder.core.effect.android.navigation.fragment.FragmentNavigateUseCase
import com.nlab.reminder.core.effect.android.navigation.fragment.util.FragmentNavigateUseCase
import com.nlab.reminder.domain.common.effect.android.navigation.*
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
        navigateAllEnd: AllEndNavigationEffectRunner,
        navigateTagEnd: TagEndNavigationEffectRunner
    ): FragmentNavigateUseCase = FragmentNavigateUseCase { navController, message ->
        when (message) {
            is TodayEndNavigationMessage -> navigateTodayEnd(navController)
            is TimetableEndNavigationMessage -> navigateTimeTable(navController)
            is AllEndNavigationMessage -> navigateAllEnd(navController)
            is TagEndNavigationMessage -> navigateTagEnd(navController, message.tag)
            else -> Unit
        }
    }

    @Provides
    fun provideTodayEndNavigate() = TodayEndNavigationEffectRunner()

    @Provides
    fun provideTimetableEndNavigate() = TimetableEndNavigationEffectRunner()

    @Provides
    fun provideAllEndNavigate() = AllEndNavigationEffectRunner()

    @Provides
    fun provideTagEndNavigate() = TagEndNavigationEffectRunner()
}