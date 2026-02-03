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

package com.nlab.reminder.core.component.schedulelist.content.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.androidx.compose.ui.ColorPressButton
import com.nlab.reminder.core.androidx.compose.ui.throttleClick
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemComposeViewBinding
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme
import com.nlab.reminder.core.designsystem.compose.theme.DimenIds
import com.nlab.reminder.core.kotlin.NonNegativeInt
import com.nlab.reminder.core.kotlin.toNonNegativeInt
import com.nlab.reminder.core.translation.StringIds

/**
 * @author Thalys
 */
internal class ClearableCompletedSubHeadlineViewHolder(
    binding: LayoutScheduleAdapterItemComposeViewBinding,
    private val onClearClicked: () -> Unit
) : ScheduleAdapterItemViewHolder(binding.root) {
    private val composeView = binding.composeView.apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
    }

    fun bind(item: ScheduleListItem.ClearableCompletedSubHeadline) {
        composeView.setContent { 
            PlaneatTheme {
                ClearableCompletedScheduleItem(
                    completedScheduleCount = item.completedScheduleCount,
                    onClearClicked = onClearClicked,
                )
            }
        }
    }
}

@Composable
private fun ClearableCompletedScheduleItem(
    completedScheduleCount: NonNegativeInt,
    onClearClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = modifier
                .padding(
                    vertical = 10.dp,
                    horizontal = dimensionResource(DimenIds.horizontal_padding_medium)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(StringIds.content_completed_with_count, completedScheduleCount.value),
                color = PlaneatTheme.colors.content1,
                style = PlaneatTheme.typography.titleSmall
            )
            Text(
                modifier = Modifier.padding(horizontal = 5.dp),
                text = stringResource(StringIds.symbol_bullet),
                color = PlaneatTheme.colors.content1,
                style = PlaneatTheme.typography.titleSmall
            )
            ColorPressButton(
                contentColor = PlaneatTheme.colors.point1,
                onClick = throttleClick(onClick = onClearClicked),
            ) { color ->
                Text(
                    text = stringResource(StringIds.clear),
                    color = color,
                    style = PlaneatTheme.typography.titleSmall
                )
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = PlaneatTheme.colors.bgLine1
        )
    }
}

@Previews
@Composable
private fun ClearableCompletedScheduleItemPreview() {
    PlaneatTheme {
        Box(
            modifier = Modifier
                .height(150.dp)
                .background(PlaneatTheme.colors.bg1Layer)
        ) {
            ClearableCompletedScheduleItem(
                completedScheduleCount = 5.toNonNegativeInt(),
                onClearClicked = {}
            )
        }
    }
}