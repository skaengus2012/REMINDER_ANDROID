/*
 * Copyright (C) 2026 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.bottomappbar.ui

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme

/**
 * @param alpha Lambda returning the layer alpha in range 0.0..1.0. It is read in the layer phase only,
 * so scrolling updates the layer property without redrawing the static rects.
 * @author Doohyun
 */
@Composable
fun BottomBlurLayer(
    alpha: () -> Float,
    modifier: Modifier = Modifier,
    containerColor: Color = PlaneatTheme.colors.bg1Layer,
    lineColor: Color = PlaneatTheme.colors.bgLine1,
    defaultContainerAlpha: Float = 0.925f
) {
    Spacer(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha().coerceIn(0f, 1f)
                compositingStrategy = CompositingStrategy.ModulateAlpha
            }
            .drawBehind {
                drawRect(color = containerColor.copy(alpha = defaultContainerAlpha))
                drawRect(color = lineColor, size = Size(size.width, 0.5.dp.roundToPx().toFloat()))
            }
    )
}

@Previews
@Composable
private fun BottomBlurLayerPreview() {
    PlaneatTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(PlaneatTheme.colors.bg1)
        ) {
            Box(modifier = Modifier.height(50.dp)) {
                BottomBlurLayer(
                    modifier = Modifier.fillMaxWidth(),
                    alpha = { 1f }
                )

                Text(
                    modifier = Modifier.align(Alignment.Center),
                    style = PlaneatTheme.typography.titleMedium,
                    color = PlaneatTheme.colors.content1,
                    text = "Hello BottomAppbar"
                )
            }
        }
    }
}
