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

package com.nlab.reminder.core.androidx.compose.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

/**
 * @author Thalys
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoxScope.ButtonBackground(
    onClick: () -> Unit,
    onClickLabel: String?,
    indication: Indication?,
) {
    Spacer(
        modifier = Modifier
            .matchParentSize()
            .combinedClickable(
                remember { MutableInteractionSource() },
                indication = indication,
                onClick = onClick,
                onClickLabel = onClickLabel,
                role = Role.Button
            )
    )
}