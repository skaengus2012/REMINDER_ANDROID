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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme

/**
 * Bottom app bar that handles [WindowInsets.Companion.navigationBars] and [WindowInsets.Companion.displayCutout] internally.
 * Callers should NOT apply navigation bar insets separately on the container.
 *
 * @author Doohyun
 */
@Composable
fun BottomAppbar(
    bottomAppbarState: BottomAppbarState,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Box(modifier = modifier) {
        BottomBlurLayer(
            modifier = Modifier.matchParentSize(),
            alphaProvider = { bottomAppbarState.backgroundAlpha * 0.925f }
        )
        Column {
            Row(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                    .height(BottomAppbarDefaults.Height)
                    .padding(horizontal = 10.dp),
                content = content
            )
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Previews
@Composable
private fun BottomAppbarPreview() {
    PlaneatTheme {
        Box(
            modifier = Modifier
                .background(color = PlaneatTheme.colors.bg1Layer)
                .fillMaxWidth()
        ) {
            val state = object : BottomAppbarState {
                override val backgroundAlpha: Float = 1f
            }
            BottomAppbar(bottomAppbarState = state) {
                NewPlanButton(onClick = {})
            }
        }
    }
}
