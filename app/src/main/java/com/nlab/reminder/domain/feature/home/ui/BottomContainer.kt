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

package com.nlab.reminder.domain.feature.home.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme

/**
 * @author Doohyun
 */
@Composable
internal fun BottomContainer(
    contentPaddingBottom: Dp,
    contentScrollState: ScrollState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var computedHeightToPx by remember { mutableStateOf(0f) }
    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                computedHeightToPx = coordinates.size.height.toFloat()
            }
    ) {
        if (computedHeightToPx != 0f) {
            AnimatedBottomContainerBackground(
                containerHeightToPx = computedHeightToPx,
                contentPaddingBottomToPx = with(LocalDensity.current) { contentPaddingBottom.toPx() },
                contentScrollState = contentScrollState
            )
        }
        content()
    }
}

@Composable
private fun AnimatedBottomContainerBackground(
    containerHeightToPx: Float,
    contentPaddingBottomToPx: Float,
    contentScrollState: ScrollState
) {
    BottomContainerBackground(
        alphaState = remember(containerHeightToPx, contentPaddingBottomToPx, contentScrollState) {
            val maxBottomContainerAnimPx = contentPaddingBottomToPx - containerHeightToPx
            derivedStateOf {
                if (contentScrollState.maxValue == Int.MAX_VALUE) 0f
                else {
                    val remainScrollToPx = contentScrollState.maxValue - contentScrollState.value
                    val visibleClipToPaddingHeight =
                        maxOf(contentPaddingBottomToPx - remainScrollToPx, 0f)
                    val bottomContainerAnimPx = maxOf(visibleClipToPaddingHeight - containerHeightToPx, 0f)
                    1f - bottomContainerAnimPx / maxBottomContainerAnimPx
                }
            }
        }
    )
}

/**
 * @param alphaState alphaState is 0f ~ 1f
 */
@Composable
private fun BottomContainerBackground(alphaState: State<Float>) {
    val containerColor = PlaneatTheme.colors.bgCard1
    val lineColor = PlaneatTheme.colors.bgLine1
    Box(modifier = Modifier.fillMaxSize()) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind { drawRect(color = containerColor.copy(alpha = 0.925f * alphaState.value)) }
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .align(Alignment.TopCenter)
                .drawBehind { drawRect(color = lineColor.copy(alpha = alphaState.value)) }
        )
    }
}