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

package com.nlab.reminder.core.component.tag.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nlab.reminder.core.androidx.compose.ui.tooling.preview.Previews
import com.nlab.reminder.core.androidx.compose.ui.throttleClick
import com.nlab.reminder.core.designsystem.compose.theme.PlaneatTheme
import com.nlab.reminder.core.translation.StringIds

/**
 * @author Thalys
 */
@Composable
fun TagCard(
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
            text = stringResource(StringIds.format_tag, text),
            style = PlaneatTheme.typography.bodyMedium,
            color = PlaneatTheme.colors.contentTag,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

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
            .background(PlaneatTheme.colors.bgTag)
            .combinedClickable(
                interactiveSource,
                indication = ripple(color = PlaneatTheme.colors.bgTagRipple),
                onClick = throttleClick(onClick = onClick),
                onClickLabel = onClickLabel,
                onLongClick = onLongClick,
                onLongClickLabel = onLongClickLabel
            )
    )
}

@Previews
@Composable
private fun TagCardPreview() {
    PlaneatTheme {
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(PlaneatTheme.colors.bgCard1)) {
            TagCard(
                modifier = Modifier.align(Alignment.Center),
                text = "Hello TagCard"
            )
        }
    }
}