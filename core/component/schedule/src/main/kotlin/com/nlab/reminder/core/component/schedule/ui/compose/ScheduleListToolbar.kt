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

package com.nlab.reminder.core.component.schedule.ui.compose

import androidx.annotation.FloatRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.androidx.compose.ui.IconButton
import com.nlab.reminder.core.androidx.compose.ui.throttleClick
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.designsystem.compose.theme.DrawableIds
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme
import com.nlab.reminder.core.translation.StringIds

/**
 * @author Thalys
 */
@Composable
fun ScheduleListToolbar(
    title: String,
    isTitleVisible: Boolean,
    @FloatRange(from = 0.0, to = 1.0) backgroundAlpha: Float,
    onBackClicked: () -> Unit,
    onMenuClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = PlaneatTheme.colors.bgCard1
    Box(modifier) {
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .drawBehind { drawRect(color = containerColor.copy(alpha = 0.96f * backgroundAlpha)) }
        )

        ScheduleListToolbarContent(
            modifier = Modifier
                .statusBarsPadding()
                .displayCutoutPadding()
                .fillMaxWidth()
                .height(48.dp),
            title = title,
            isTitleVisible = isTitleVisible,
            onBackClicked = onBackClicked,
            onMenuClicked = onMenuClicked
        )
    }
}

@Composable
private fun ScheduleListToolbarContent(
    title: String,
    isTitleVisible: Boolean,
    onBackClicked: () -> Unit,
    onMenuClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Title(
            modifier = Modifier.align(Alignment.Center),
            title = title,
            isVisible = isTitleVisible
        )

        BackButton(
            modifier = Modifier
                .align(Alignment.CenterStart),
            onClick = throttleClick(onClick = onBackClicked),
        )

        IconButton(
            modifier = Modifier
                .align(Alignment.CenterEnd),
            onClick = throttleClick(onClick = onMenuClicked),
            painter = painterResource(DrawableIds.ic_more),
            contentDescription = stringResource(StringIds.content_description_more),
            tint = PlaneatTheme.colors.point1,
        )
    }
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .wrapContentWidth()
            .height(48.dp)
            .clickable(
                onClick = throttleClick(onClick = onClick),
                onClickLabel = stringResource(StringIds.content_description_back),
                role = Role.Button
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(start = 3.dp),
            painter = painterResource(DrawableIds.ic_back),
            contentDescription = null,
            tint = PlaneatTheme.colors.point1,
        )
        Text(
            modifier = Modifier.padding(start = 3.dp),
            text = stringResource(StringIds.label_lists),
            style = PlaneatTheme.typography.bodyLarge,
            color = PlaneatTheme.colors.point1,
        )
    }
}

@Composable
private fun Title(
    title: String,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = fadeIn(tween(durationMillis = 200)),
        exit = fadeOut(tween(durationMillis = 200)),
    ) {
        Text(
            text = title,
            style = PlaneatTheme.typography.titleMedium,
            color = PlaneatTheme.colors.content1,
        )
    }
}

@Previews
@Composable
private fun ScheduleListToolbarPreview() {
    PlaneatTheme {
        ScheduleListToolbar(
            title = "Today",
            isTitleVisible = true,
            backgroundAlpha = 1.0f,
            onBackClicked = {},
            onMenuClicked = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}