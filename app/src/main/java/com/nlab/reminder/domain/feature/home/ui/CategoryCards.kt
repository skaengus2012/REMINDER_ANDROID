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

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nlab.reminder.R
import com.nlab.reminder.core.androidx.compose.ui.throttle
import com.nlab.reminder.core.android.designsystem.icon.ReminderIcons
import com.nlab.reminder.core.android.resources.font.CategoryCountFontFamily
import com.nlab.reminder.core.designsystem.compose.theme.ReminderTheme
import com.nlab.reminder.core.android.resources.icon.IcHomeCategoryAll
import com.nlab.reminder.core.android.resources.icon.IcHomeCategoryTimetable
import com.nlab.reminder.core.android.resources.icon.IcHomeCategoryToday

/**
 * @author Doohyun
 */
@Composable
internal fun CategoryCardsRow(
    todayCount: Long,
    timetableCount: Long,
    allCount: Long,
    modifier: Modifier = Modifier,
    onTodayCategoryClicked: () -> Unit = {},
    onTimetableCategoryClicked: () -> Unit = {},
    onAllCategoryClicked: () -> Unit = {}
) {
    Row(modifier = modifier.fillMaxWidth()) {
        TodayCategoryCard(
            modifier = Modifier.weight(1f),
            remainCount = todayCount,
            onClick = onTodayCategoryClicked
        )

        Spacer(modifier = Modifier.width(14.dp))

        TimetableCategoryCard(
            modifier = Modifier.weight(1f),
            remainCount = timetableCount,
            onClick = onTimetableCategoryClicked
        )

        Spacer(modifier = Modifier.width(14.dp))

        AllCategoryCard(
            modifier = Modifier.weight(1f),
            remainCount = allCount,
            onClick = onAllCategoryClicked
        )
    }
}

@Composable
private fun TodayCategoryCard(
    remainCount: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    BasicCategoryCard(
        name = stringResource(R.string.home_category_today),
        remainCount = remainCount,
        icon = {
            Image(
                imageVector = ReminderIcons.IcHomeCategoryToday,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.4883f)
                    .aspectRatio(1f)
            )
        },
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
private fun TimetableCategoryCard(
    remainCount: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    BasicCategoryCard(
        name = stringResource(R.string.home_category_timetable),
        remainCount = remainCount,
        icon = {
            // case1: If you use webp, the image quality is not good when in landscape mode.
            // case2: When using svg, the square is not drawn properly. (No problem when using view system)
            // case3: Resolve when using image vector
            Image(
                imageVector = ReminderIcons.IcHomeCategoryTimetable,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .aspectRatio(1f)
            )
        },
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
private fun AllCategoryCard(
    remainCount: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    BasicCategoryCard(
        name = stringResource(R.string.home_category_timetable),
        remainCount = remainCount,
        icon = {
            Image(
                imageVector = ReminderIcons.IcHomeCategoryAll,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.4232f)
                    .aspectRatio(18.22f / 15.93f)
            )
        },
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
private fun BasicCategoryCard(
    name: String,
    remainCount: Long,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onClickLabel: String = name,
) {
    Box(modifier = modifier) {
        CategoryCardBackground(onClick, onClickLabel)
        Column(
            modifier = Modifier.matchParentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.1346f))
            CategoryIcon(icon)
            Spacer(modifier = Modifier.fillMaxHeight(0.048f))
            CategoryTitleText(name)
            Spacer(modifier = Modifier.fillMaxHeight(0.17f))
            CategoryCountText(remainCount)
        }
    }
}

@Composable
private fun CategoryCardBackground(
    onClick: () -> Unit = {},
    onClickLabel: String? = null
) {
    Spacer(
        modifier = Modifier
            .aspectRatio(1 / 1.625f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = ReminderTheme.colors.bgCard1Ripple),
                onClick = onClick.throttle(),
                onClickLabel = onClickLabel,
                role = Role.Tab
            )
            .background(ReminderTheme.colors.bgCard1)
    )
}

@Composable
private fun CategoryIcon(icon: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.4479f)
            .aspectRatio(1f)
            .background(ReminderTheme.colors.bg1, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
private fun CategoryTitleText(text: String) {
    Text(
        text = text,
        style = ReminderTheme.typography.bodyMedium,
        color = ReminderTheme.colors.content2
    )
}

@Composable
private fun CategoryCountText(count: Long) {
    Text(
        text = count.toString(),
        style = ReminderTheme.typography.bodyLarge,
        color = ReminderTheme.colors.content1,
        fontFamily = CategoryCountFontFamily
    )
}

@Preview(
    name = "LightCategoryCardsRowPreview",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkCategoryCardsRowPreview",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
private fun CategoryCardSectionPreview() {
    ReminderTheme {
        CategoryCardsRow(
            todayCount = 10,
            timetableCount = 20,
            allCount = 30,
        )
    }
}