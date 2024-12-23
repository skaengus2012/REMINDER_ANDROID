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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.nlab.reminder.core.android.widget.Toast

/**
 * @author Thalys
 */
@Stable
class PlaneatAppState(
    val navController: NavHostController,
    val appToast: Toast,
)

@Composable
fun rememberPlaneatAppState(
    navController: NavHostController = rememberNavController(),
    appToast: Toast,
): PlaneatAppState = PlaneatAppState(
    navController = navController,
    appToast = appToast
)