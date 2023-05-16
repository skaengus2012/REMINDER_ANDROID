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

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nlab.reminder.R
import com.nlab.reminder.core.android.designsystem.theme.ReminderTheme

/**
 * @author Doohyun
 */
@Composable
fun TagText(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onClickLabel: String? = null,
    onLongClick: () -> Unit = {},
    onLongClickLabel: String? = null
) {
    Box(modifier) {
        TagTextBackground(
            onClick = onClick,
            onClickLabel = onClickLabel,
            onLongClick = onLongClick,
            onLongClickLabel = onLongClickLabel
        )
        Text(
            text = LocalContext.current.getString(R.string.format_tag, text),
            fontSize = 14.sp,
            color = ReminderTheme.colors.fontTag,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BoxScope.TagTextBackground(
    onClickLabel: String? = null,
    onLongClickLabel: String? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val interactiveSource = remember { MutableInteractionSource() }
    Spacer(
        modifier = Modifier
            .matchParentSize()
            .clip(RoundedCornerShape(topStart = 17.5.dp, topEnd = 17.5.dp, bottomEnd = 0.dp, bottomStart = 17.5.dp))
            .background(ReminderTheme.colors.bgTag)
            .combinedClickable(
                interactiveSource,
                indication = rememberRipple(color = ReminderTheme.colors.bgRipple1),
                onClick = onClick,
                onClickLabel = onClickLabel,
                onLongClick = onLongClick,
                onLongClickLabel = onLongClickLabel
            )
    )
}

@Preview(
    name = "DarkTagTextPreview",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Preview(
    name = "LightTagTextPreview",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Composable
private fun TagTextPreview() {
    ReminderTheme {
        Box(
            modifier = Modifier.background(ReminderTheme.colors.bgCard1)
        ) {
            TagText(text = "Hello")
        }
    }
}