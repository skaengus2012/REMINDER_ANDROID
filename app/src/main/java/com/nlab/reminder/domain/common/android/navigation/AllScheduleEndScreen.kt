package com.nlab.reminder.domain.common.android.navigation

import androidx.navigation.NavController
import com.nlab.reminder.core.android.navigation.Screen

/**
 * @author Doohyun
 */
object AllScheduleEndScreen : Screen

fun NavController.navigateToAllScheduleEnd() {
    context.navGraphEntryPoint()
        .navControllerGraph()
        .navigate(navHandle = this, AllScheduleEndScreen)
}