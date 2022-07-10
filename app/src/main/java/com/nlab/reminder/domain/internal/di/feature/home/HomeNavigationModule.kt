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

package com.nlab.reminder.domain.internal.di.feature.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.nlab.reminder.core.effect.message.navigation.android.NavigationMediator
import com.nlab.reminder.core.effect.message.navigation.android.util.NavigationMediator
import com.nlab.reminder.core.entrypoint.util.EntryBlock
import com.nlab.reminder.domain.feature.home.*
import com.nlab.reminder.domain.feature.home.view.HomeFragmentDirections
import com.nlab.reminder.domain.feature.home.tag.config.view.HomeTagConfigDialogFragment
import com.nlab.reminder.domain.feature.home.tag.rename.view.HomeTagRenameDialogFragment
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
    fun provideFragmentNavigateUseCase(
        navigateWithGlobalAction: NavigationMediator
    ): NavigationMediator = NavigationMediator { navController, message ->
        when (message) {
            is HomeTagConfigNavigationMessage ->
                HomeFragmentDirections
                    .actionHomeFragmentToHomeConfigDialogFragment(REQUEST_KEY_HOME_TO_HOME_TAG_CONFIG, message.tag)
                    .run(navController::navigate)

            is HomeTagRenameNavigationMessage -> {
                HomeFragmentDirections
                    .actionHomeFragmentToHomeTagRenameDialogFragment(REQUEST_KEY_HOME_TO_HOME_TAG_RENAME, message.tag)
                    .run(navController::navigate)
            }

            else -> navigateWithGlobalAction(navController, message)
        }
    }

    @HomeScope
    @IntoSet
    @Provides
    fun provideHomeFragmentResultReceiver(fragment: Fragment) = EntryBlock {
        val viewModel: HomeViewModel by fragment.viewModels()
        fragment.setFragmentResultListener(
            requestKey = REQUEST_KEY_HOME_TO_HOME_TAG_CONFIG,
            listener = HomeTagConfigDialogFragment.resultListenerOf(
                onRenameClicked = { tag -> viewModel.onTagRenameRequestClicked(tag) },
                onDeleteClicked = { tag -> viewModel.onTagDeleteRequestClicked(tag) }
            )
        )
        fragment.setFragmentResultListener(
            requestKey = REQUEST_KEY_HOME_TO_HOME_TAG_RENAME,
            listener = HomeTagRenameDialogFragment.resultListenerOf(
                onConfirmClicked = { tag, rename -> viewModel.onTagRenameConfirmClicked(tag, rename) }
            )
        )
    }

    companion object {
        private const val REQUEST_KEY_HOME_TO_HOME_TAG_CONFIG = "requestKeyHomeToHomeTagConfig"
        private const val REQUEST_KEY_HOME_TO_HOME_TAG_RENAME = "requestKeyHomeToHomeTagRename"
    }
}