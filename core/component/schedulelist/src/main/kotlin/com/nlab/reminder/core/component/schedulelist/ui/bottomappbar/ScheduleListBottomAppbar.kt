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

package com.nlab.reminder.core.component.schedulelist.ui.bottomappbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.component.bottomappbar.ui.BottomAppbar
import com.nlab.reminder.core.component.bottomappbar.ui.NewPlanButton
import com.nlab.reminder.core.designsystem.compose.theme.DrawableIds
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme

/**
 * @author Thalys
 */
@Composable
fun ScheduleListBottomAppbar(
    bottomAppbarState: ScheduleListBottomAppbarState,
    isMultiSelectionEnabled: Boolean,
    isMultiSelectionContentEnabled: Boolean,
    onTimingConfigClicked: () -> Unit,
    onCompleteClicked: () -> Unit,
    onTagConfigClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onNewPlanClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    BottomAppbar(
        bottomAppbarState = bottomAppbarState,
        modifier = modifier
    ) {
        if (isMultiSelectionEnabled) {
            val tint =
                if (isMultiSelectionContentEnabled) PlaneatTheme.colors.point1
                else PlaneatTheme.colors.content2Hint
            SelectionActionItem(
                iconRes = DrawableIds.ic_calendar_clock_24,
                contentDescription = null,
                enabled = isMultiSelectionContentEnabled,
                tint = tint,
                onClick = onTimingConfigClicked,
            )
            SelectionActionItem(
                iconRes = DrawableIds.ic_done_all_24,
                contentDescription = null,
                enabled = isMultiSelectionContentEnabled,
                tint = tint,
                onClick = onCompleteClicked,
            )
            SelectionActionItem(
                iconRes = DrawableIds.ic_hash_tag_24,
                contentDescription = null,
                enabled = isMultiSelectionContentEnabled,
                tint = tint,
                onClick = onTagConfigClicked,
            )
            SelectionActionItem(
                iconRes = DrawableIds.ic_trash_24,
                contentDescription = null,
                enabled = isMultiSelectionContentEnabled,
                tint = tint,
                onClick = onDeleteClicked,
            )
        } else {
            NewPlanButton(
                onClick = onNewPlanClicked
            )
        }
    }
}

@Composable
private fun RowScope.SelectionActionItem(
    iconRes: Int,
    contentDescription: String?,
    enabled: Boolean,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 24.dp),
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Previews
@Composable
private fun ScheduleListBottomAppbarNormalPreview() {
    PlaneatTheme {
        ScheduleListBottomAppbar(
            bottomAppbarState = rememberScheduleListBottomAppbarState(initialBackgroundAlpha = 1f),
            isMultiSelectionEnabled = false,
            isMultiSelectionContentEnabled = false,
            onTimingConfigClicked = {},
            onCompleteClicked = {},
            onTagConfigClicked = {},
            onDeleteClicked = {},
            onNewPlanClicked = {}
        )
    }
}

@Previews
@Composable
private fun ScheduleListBottomAppbarMultiSelectEmptyPreview() {
    PlaneatTheme {
        ScheduleListBottomAppbar(
            bottomAppbarState = rememberScheduleListBottomAppbarState(initialBackgroundAlpha = 1f),
            isMultiSelectionEnabled = true,
            isMultiSelectionContentEnabled = false,
            onTimingConfigClicked = {},
            onCompleteClicked = {},
            onTagConfigClicked = {},
            onDeleteClicked = {},
            onNewPlanClicked = {}
        )
    }
}

@Previews
@Composable
private fun ScheduleListBottomAppbarMultiSelectSelectedPreview() {
    PlaneatTheme {
        ScheduleListBottomAppbar(
            bottomAppbarState = rememberScheduleListBottomAppbarState(initialBackgroundAlpha = 1f),
            isMultiSelectionEnabled = true,
            isMultiSelectionContentEnabled = true,
            onTimingConfigClicked = {},
            onCompleteClicked = {},
            onTagConfigClicked = {},
            onDeleteClicked = {},
            onNewPlanClicked = {}
        )
    }
}
