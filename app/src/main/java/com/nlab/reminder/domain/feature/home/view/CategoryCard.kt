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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nlab.reminder.R
import com.nlab.reminder.core.android.designsystem.theme.FontDangamAsac
import com.nlab.reminder.core.android.designsystem.theme.ReminderTheme

/**
 * @author Doohyun
 */
@Composable
internal fun TodayCategoryCard(
    remainCount: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    CategoryCard(
        name = LocalContext.current.getString(R.string.home_category_today),
        remainCount = remainCount,
        icon = {
            Image(
                painter = painterResource(id = R.drawable.ic_home_category_today),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.4479f)
                    .aspectRatio(1f)
            )
        },
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
internal fun TimetableCategoryCard(
    remainCount: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    CategoryCard(
        name = LocalContext.current.getString(R.string.home_category_timetable),
        remainCount = remainCount,
        icon = {
            Image(
                painter = painterResource(id = R.drawable.ic_home_category_timetable),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.4243f)
                    .aspectRatio(18.6f / 19.28f)
            )
        },
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
internal fun AllCategoryCard(
    remainCount: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    CategoryCard(
        name = LocalContext.current.getString(R.string.home_category_timetable),
        remainCount = remainCount,
        icon = {
            Image(
                painter = painterResource(id = R.drawable.ic_home_category_all),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.4237f)
                    .aspectRatio(18.22f / 15.93f)
            )
        },
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
private fun CategoryCard(
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
    val interactiveSource = remember { MutableInteractionSource() }
    Spacer(
        modifier = Modifier
            .aspectRatio(1 / 1.625f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactiveSource,
                indication = rememberRipple(color = ReminderTheme.colors.bgRipple1),
                onClick = onClick,
                onClickLabel = onClickLabel
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
        fontSize = 14.sp,
        color = ReminderTheme.colors.font2
    )
}

@Composable
private fun CategoryCountText(count: Long) {
    Text(
        text = count.toString(),
        fontSize = 21.5.sp,
        color = ReminderTheme.colors.font1,
        fontFamily = FontDangamAsac
    )
}

@Preview(
    name = "DarkCategoryCardsPreview",
    uiMode = UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Preview(
    name = "LightCategoryCardsPreview",
    uiMode = UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Composable
private fun CategoryCardsPreview() {
    ReminderTheme {
        Row {
            TodayCategoryCard(
                remainCount = 10,
                modifier = Modifier.width(100.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            TimetableCategoryCard(
                remainCount = 5,
                modifier = Modifier.width(100.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            AllCategoryCard(
                remainCount = 28,
                modifier = Modifier.width(100.dp)
            )
        }
    }
}