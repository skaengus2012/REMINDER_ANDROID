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

package com.nlab.reminder.core.designsystem.compose.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme

/**
 * @author Doohyun
 */
@Composable
fun PlaneatLoading(
    modifier: Modifier = Modifier,
    color: Color = PlaneatTheme.colors.point1,
) {
    CircularProgressIndicator(
        modifier = modifier.size(36.dp),
        color = color,
        strokeWidth = 3.5.dp
    )
}

@Composable
fun PlaneatLoadingContent(
    modifier: Modifier = Modifier,
    color: Color = PlaneatTheme.colors.point1,
) {
    Box(modifier = modifier.fillMaxSize()) {
        PlaneatLoading(
            modifier = Modifier.align(Alignment.Center),
            color = color,
        )
    }
}