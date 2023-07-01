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

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nlab.reminder.R
import com.nlab.reminder.core.android.designsystem.theme.ReminderTheme
import com.nlab.reminder.domain.common.data.model.Tag
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * @author Doohyun
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TagTextsBox(
    tags: ImmutableList<Tag>,
    modifier: Modifier = Modifier,
    onTagClicked: (Tag) -> Unit = {},
    onTagLongClicked: (Tag) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 160.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ReminderTheme.colors.bgCard1),
    ) {
        if (tags.isEmpty()) {
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = LocalContext.current.getString(R.string.common_tag_empty),
                    fontSize = 14.sp,
                    color = ReminderTheme.colors.font2,
                )
            }
        } else {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 11.5.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.Start)
            ) {
                tags.forEach { tag ->
                    key(tag.tagId) {
                        TagText(
                            text = tag.name,
                            modifier = Modifier.padding(vertical = 6.5.dp),
                            onClick = { onTagClicked(tag) },
                            onLongClick = { onTagLongClicked(tag) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    name = "DarkTagTextsBoxPreview",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Preview(
    name = "LightTagTextsBoxPreview",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Composable
private fun TagTextsBoxPreview() {
    ReminderTheme {
        TagTextsBox(
            persistentListOf(
                Tag(tagId = 1, name = "MyTag"),
                Tag(tagId = 2, name = "YourTag"),
                Tag(tagId = 3, name = "OurTag")
            )
        )
    }
}

@Preview(
    name = "DarkTagTextsBoxEmptyPreview",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Preview(
    name = "LightTagTextsBoxEmptyPreview",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Composable
private fun TagTextsBoxEmptyPreview() {
    ReminderTheme {
        TagTextsBox(persistentListOf())
    }
}