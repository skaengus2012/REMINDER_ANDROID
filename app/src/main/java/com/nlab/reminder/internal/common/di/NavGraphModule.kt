package com.nlab.reminder.internal.common.di

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.navigation.NavController
import com.nlab.reminder.R
import com.nlab.reminder.core.android.navigation.NavGraph
import com.nlab.reminder.domain.common.android.navigation.AllScheduleEndScreen
import com.nlab.reminder.domain.common.android.navigation.OpenLinkScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
class NavGraphModule {
    @Singleton
    @Provides
    fun provideNavControllerGraph(): NavGraph<NavController> = NavGraph {
        node<AllScheduleEndScreen> {
            navHandle.navigate(R.id.action_global_allScheduleFragment)
        }
    }

    @Singleton
    @Provides
    fun provideContextGraph(): NavGraph<Context> = NavGraph {
        node<OpenLinkScreen> { screen ->
            navHandle.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(screen.link)))
        }
    }
}