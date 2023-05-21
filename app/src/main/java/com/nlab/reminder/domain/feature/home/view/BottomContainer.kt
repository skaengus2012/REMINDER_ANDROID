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

package com.nlab.reminder.domain.feature.home.view

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nlab.reminder.domain.common.android.designsystem.theme.ReminderTheme

/**
 * @author Doohyun
 */
@Composable
internal fun BottomContainer(
    containerHeight: Dp,
    contentBottomPadding: Dp,
    contentScrollState: ScrollState,
) {
    AnimatedBottomContainer(
        containerHeightToPx = with(LocalDensity.current) { containerHeight.toPx() },
        contentBottomPaddingToPx = with(LocalDensity.current) { contentBottomPadding.toPx() },
        scrollState = contentScrollState,
        modifier = Modifier
            .fillMaxWidth()
            .height(containerHeight)
    )
}

@Composable
private fun AnimatedBottomContainer(
    containerHeightToPx: Float,
    contentBottomPaddingToPx: Float,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    // offset is 0f ~ 1f
    val offset by remember(scrollState) {
        val maxBottomContainerAnimPx = contentBottomPaddingToPx - containerHeightToPx
        derivedStateOf {
            if (scrollState.maxValue == Int.MAX_VALUE) 0f
            else {
                val remainScrollToPx = scrollState.maxValue - scrollState.value
                val visibleClipToPaddingHeight =
                    maxOf(contentBottomPaddingToPx - remainScrollToPx, 0f)
                val bottomContainerAnimPx = maxOf(visibleClipToPaddingHeight - containerHeightToPx, 0f)
                1f - bottomContainerAnimPx / maxBottomContainerAnimPx
            }
        }
    }
    BottomContainerSpace(modifier = modifier.alpha(offset))
}

@Composable
private fun BottomContainerSpace(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .background(ReminderTheme.colors.bgCard1.copy(alpha = 0.925f))
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .align(Alignment.TopCenter)
                .background(ReminderTheme.colors.bgLine1)
        )
    }
}

@Preview(
    name = "LightBottomContainerSpaceContainerPreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkBottomContainerSpaceContainerPreview",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun BottomContainerSpaceContainerPreview() {
    ReminderTheme {
        Surface(color = Color.Red) {
            BottomContainerSpace(
                modifier = Modifier
                    .height(50.dp)
                    .alpha(0.6f)
            )
        }
    }
}