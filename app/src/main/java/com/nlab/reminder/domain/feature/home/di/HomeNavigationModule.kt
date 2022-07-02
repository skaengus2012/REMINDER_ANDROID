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
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.nlab.reminder.core.effect.message.navigation.NavigationEffectReceiver
import com.nlab.reminder.core.effect.message.navigation.android.util.fragment.NavigationMessageReceiver
import com.nlab.reminder.core.effect.message.navigation.android.util.fragment.util.FragmentNavigateEffectReceiver
import com.nlab.reminder.core.effect.message.navigation.android.util.fragment.util.NavigationMessageReceiver
import com.nlab.reminder.core.entrypoint.fragment.util.EntryBlock
import com.nlab.reminder.domain.feature.home.*
import com.nlab.reminder.domain.feature.home.view.HomeTagConfigDialogFragment
import com.nlab.reminder.domain.feature.home.view.HomeTagConfigNavigationEffectRunner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.multibindings.IntoSet

/**
 * @author Doohyun
 */
@Module
@InstallIn(FragmentComponent::class)
class HomeNavigationModule {
    @HomeScope
    @Provides
    fun provideHomeNavigationEffectRunner() = HomeTagConfigNavigationEffectRunner()

    @HomeScope
    @Provides
    fun provideFragmentNavigateUseCase(
        navigateWithGlobalAction: NavigationMessageReceiver,
        @HomeScope navigateTagConfig: HomeTagConfigNavigationEffectRunner
    ): NavigationMessageReceiver = NavigationMessageReceiver { navController, message ->
        when (message) {
            is HomeTagConfigNavigationMessage -> navigateTagConfig(navController, message.tag)
            else -> navigateWithGlobalAction(navController, message)
        }
    }

    @HomeScope
    @Provides
    fun provideNavigationEffectReceiver(
        fragment: Fragment,
        @HomeScope fragmentNavigationUseCase: NavigationMessageReceiver
    ): NavigationEffectReceiver = FragmentNavigateEffectReceiver(fragment, fragmentNavigationUseCase)

    @HomeScope
    @IntoSet
    @Provides
    fun provideHomeFragmentResultReceiver(
        fragment: Fragment
    ) = EntryBlock {
        val viewModel: HomeViewModel by fragment.viewModels()
        fragment.setFragmentResultListener(
            requestKey = HomeTagConfigDialogFragment.RESULT_KEY,
            listener = HomeTagConfigDialogFragment.resultListenerOf { resultKey, tag ->
                when (resultKey) {
                    HomeTagConfigDialogFragment.RESULT_TYPE_RENAME_REQUEST -> viewModel.onTagRenameRequestClicked(tag)
                    HomeTagConfigDialogFragment.RESULT_TYPE_DELETE_REQUEST -> viewModel.onTagDeleteRequestClicked(tag)
                }
            }
        )
    }
}