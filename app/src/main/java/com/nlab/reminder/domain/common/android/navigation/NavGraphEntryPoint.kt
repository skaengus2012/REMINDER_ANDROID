package com.nlab.reminder.domain.common.android.navigation

import android.content.Context
import androidx.navigation.NavController
import com.nlab.reminder.core.android.navigation.NavGraph
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * @author Doohyun
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface NavGraphEntryPoint {
    fun navControllerGraph(): NavGraph<NavController>
    fun contextGraph(): NavGraph<Context>
}

internal fun Context.navGraphEntryPoint(): NavGraphEntryPoint =
    EntryPointAccessors
        .fromApplication(this, NavGraphEntryPoint::class.java)