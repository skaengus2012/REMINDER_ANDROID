/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.apps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.nlab.reminder.core.android.widget.Toast
import com.nlab.reminder.core.data.util.TimeZoneMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone

/**
 * @author Thalys
 */
@Stable
class PlaneatAppState(
    coroutineScope: CoroutineScope,
    timeZoneMonitor: TimeZoneMonitor,
    val navController: NavHostController,
    private val appToast: Toast,
) {
    val currentTimeZone: StateFlow<TimeZone> = timeZoneMonitor
        .currentTimeZone
        .stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(5_000),
            TimeZone.currentSystemDefault(),
        )

    fun showApplicationToast(message: String) {
        appToast.showToast(text = message)
    }
}

@Composable
fun rememberPlaneatAppState(
    timeZoneMonitor: TimeZoneMonitor,
    appToast: Toast,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController(),
): PlaneatAppState = remember(navController, appToast) {
    PlaneatAppState(
        coroutineScope = coroutineScope,
        timeZoneMonitor = timeZoneMonitor,
        navController = navController,
        appToast = appToast
    )
}