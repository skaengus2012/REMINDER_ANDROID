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

import android.content.res.Configuration.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nlab.reminder.R
import com.nlab.reminder.core.android.designsystem.theme.ReminderTheme

/**
 * @author Doohyun
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    var count by remember { mutableStateOf(0L) }
    LazyColumn(modifier) {
        item {
            Spacer(modifier = Modifier.height(37.dp))

            Logo(modifier = Modifier.padding(horizontal = 20.dp))

            Spacer(modifier = Modifier.height(42.5.dp))

            CategoryCardSection(
                modifier = Modifier.padding(horizontal = 20.dp),
                todayCount = count,
                timetableCount = 0,
                allCount = 0,
                onTodayCategoryClicked = { count++ }
            )

            Spacer(modifier = Modifier.height(59.dp))
        }
    }
}

@Composable
private fun Logo(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier
            .width(126.dp)
            .height(25.dp),
        painter = painterResource(id = R.drawable.ic_logo),
        contentDescription = null,
        colorFilter = ColorFilter.tint(ReminderTheme.colors.font1),
    )
}

@Composable
private fun HomeTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = ReminderTheme.colors.font1,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
private fun CategoryCardSection(
    todayCount: Long,
    timetableCount: Long,
    allCount: Long,
    modifier: Modifier = Modifier,
    onTodayCategoryClicked: () -> Unit = {},
    onTimetableCategoryClicked: () -> Unit = {},
    onAllCategoryClicked: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        HomeTitle(
            text = stringResource(id = R.string.home_category_header),
            modifier = Modifier.padding(bottom = 14.dp)
        )
        Row {
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
}

@Preview(
    name = "LightLogoPreview",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkLogoPreview",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
private fun LogoPreview() {
    ReminderTheme {
        Logo()
    }
}

@Preview(
    name = "LightCategoryCardSectionPreview",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkCategoryCardSectionPreview",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
private fun CategoryCardSectionPreview() {
    ReminderTheme {
        CategoryCardSection(
            todayCount = 10,
            timetableCount = 20,
            allCount = 30,
        )
    }
}