package com.nlab.reminder.domain.feature.home.di

import androidx.fragment.app.Fragment
import com.nlab.reminder.core.effect.android.navigation.NavigationEffectReceiver
import com.nlab.reminder.core.effect.android.navigation.fragment.FragmentNavigateUseCase
import com.nlab.reminder.core.effect.android.navigation.fragment.util.FragmentNavigateEffectReceiver
import com.nlab.reminder.core.effect.android.navigation.fragment.util.FragmentNavigateUseCase
import com.nlab.reminder.domain.feature.home.HomeScope
import com.nlab.reminder.domain.feature.home.HomeTagConfigNavigation
import com.nlab.reminder.domain.feature.home.view.HomeNavigationEffectRunner
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
    @Provides
    fun provideHomeNavigationEffectRunner() = HomeNavigationEffectRunner()

    @HomeScope
    @FragmentScoped
    @Provides
    fun provideFragmentNavigateUseCase(
        navigateWithGlobalAction: FragmentNavigateUseCase,
        @HomeScope featureNavigationEffectRunner: HomeNavigationEffectRunner
    ): FragmentNavigateUseCase = FragmentNavigateUseCase { navController, message ->
        when (message) {
            is HomeTagConfigNavigation ->
                featureNavigationEffectRunner.navigateHomeTagConfig(navController, message.tag)
            else -> navigateWithGlobalAction(navController, message)
        }
    }

    @HomeScope
    @FragmentScoped
    @Provides
    fun provideNavigationEffectReceiver(
        fragment: Fragment,
        @HomeScope fragmentNavigationUseCase: FragmentNavigateUseCase
    ): NavigationEffectReceiver = FragmentNavigateEffectReceiver(fragment, fragmentNavigationUseCase)
}