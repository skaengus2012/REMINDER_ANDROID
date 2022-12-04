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

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.nlab.reminder.R
import com.nlab.reminder.core.android.navigation.activity.condition
import com.nlab.reminder.core.android.navigation.navcontroller.condition
import com.nlab.reminder.core.android.navigation.util.NavigationTable
import com.nlab.reminder.core.android.widget.ToastHandle
import com.nlab.reminder.domain.common.android.navigation.AllScheduleEndNavigation
import com.nlab.reminder.domain.common.android.navigation.SafeOpenLinkNavigation
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
class NavigationModule {
    @Reusable
    @Provides
    fun provideNavControllerTable(): NavigationTable<NavController> = NavigationTable {
        condition<AllScheduleEndNavigation> { (navController) ->
            navController.navigate(R.id.action_global_allScheduleFragment)
        }
    }

    @Reusable
    @Provides
    fun provide(
        toastHandle: ToastHandle
    ): NavigationTable<FragmentActivity> = NavigationTable {
        condition<SafeOpenLinkNavigation> { (activity, navigation) ->
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(navigation.link)))
            } catch (e: Throwable) {
                Timber.e(e)
                toastHandle.showToast(navigation.errorMessageRes)
            }
        }
    }
}