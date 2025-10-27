/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.core.androidx.compose.ui

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme

/**
 * @author Thalys
 */
@Composable
fun HeadBlurLayer(
    @FloatRange(from = 0.0, to = 1.0) alpha: Float,
    modifier: Modifier = Modifier,
    containerColor: Color = PlaneatTheme.colors.bg1Layer,
    lineColor: Color = PlaneatTheme.colors.bgLine1,
    defaultContainerAlpha: Float = 0.96f
) {
    Box(modifier = modifier) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { drawRect(color = containerColor.copy(alpha = defaultContainerAlpha * alpha)) }
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .align(Alignment.BottomCenter)
                .drawBehind { drawRect(color = lineColor.copy(alpha = alpha)) }
        )
    }
}

@Previews
@Composable
private fun HeadBlurPreview() {
    PlaneatTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(PlaneatTheme.colors.bg1)
        ) {
            Box(modifier = Modifier.height(50.dp)) {
                HeadBlurLayer(
                    modifier = Modifier.fillMaxWidth(),
                    alpha = 1f
                )

                Text(
                    modifier = Modifier.align(Alignment.Center),
                    style = PlaneatTheme.typography.titleMedium,
                    color = PlaneatTheme.colors.content1,
                    text = "Hello Toolbar"
                )
            }
        }
    }
}