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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
            CategoryCardSection(
                modifier = Modifier.padding(horizontal = 19.dp),
                todayCount = count,
                onTodayCategoryClicked = { count++ }
            )
        }
    }
}

@Composable
internal fun HomeTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    println("HomeTitle")
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
    todayCount: Long = 0,
    timetableCount: Long = 0,
    allCount: Long = 0,
    onTodayCategoryClicked: () -> Unit = {},
    onTimetableCategoryClicked: () -> Unit = {},
    onAllCategoryClicked: () -> Unit = {},
    modifier: Modifier = Modifier
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

            Spacer(modifier = Modifier.fillMaxWidth(0.0388f))

            TimetableCategoryCard(
                modifier = Modifier.weight(1f),
                remainCount = timetableCount,
                onClick = onTimetableCategoryClicked
            )

            Spacer(modifier = Modifier.fillMaxWidth(0.0388f))

            AllCategoryCard(
                modifier = Modifier.weight(1f),
                remainCount = allCount,
                onClick = onAllCategoryClicked
            )
        }
    }
}

@Preview(
    name = "LightCategoryCardSectionPreviewPreview",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)
@Preview(
    name = "DarkCategoryCardSectionPreviewPreview",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Composable
private fun CategoryCardSectionPreview() {
    ReminderTheme {
        CategoryCardSection(
            todayCount = 1
        )
    }
}