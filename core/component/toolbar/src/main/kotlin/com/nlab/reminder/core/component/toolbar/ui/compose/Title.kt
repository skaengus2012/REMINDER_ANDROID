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

package com.nlab.reminder.core.component.toolbar.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme

/**
 * @author Thalys
 */
@Composable
fun Title(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.toolbarHeight()) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = text,
            style = PlaneatTheme.typography.titleMedium,
            color = PlaneatTheme.colors.content1,
        )
    }
}

@Previews
@Composable
private fun TitlePreviews() {
    PlaneatTheme {
        Box(
            modifier = Modifier
                .background(PlaneatTheme.colors.bg1)
                .size(60.dp)
        ) {
            Title(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(color = PlaneatTheme.colors.red1),
                text = "Title"
            )
        }
    }
}