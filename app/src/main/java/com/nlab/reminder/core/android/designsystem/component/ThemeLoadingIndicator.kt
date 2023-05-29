/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.core.android.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.android.designsystem.theme.ReminderTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author Doohyun
 */
@Composable
fun ThemeLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = ReminderTheme.colors.pointColor1,
    strokeWidth: Dp = 3.5.dp,
    displayDelayTime: Long = 500
) {
    val coroutineScope = rememberCoroutineScope()
    var isIndicatorVisibleState by remember { mutableStateOf(false) }
    if (isIndicatorVisibleState) {
        CircularProgressIndicator(
            modifier = modifier.size(36.dp),
            color = color,
            strokeWidth = strokeWidth
        )
    }

    DisposableEffect(true) {
        val job = coroutineScope.launch {
            delay(displayDelayTime)
            isIndicatorVisibleState = true
        }

        onDispose {
            job.cancel()
        }
    }
}