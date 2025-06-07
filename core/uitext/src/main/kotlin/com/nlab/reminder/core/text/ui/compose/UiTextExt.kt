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

package com.nlab.reminder.core.text.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.nlab.reminder.core.text.UiText
import com.nlab.reminder.core.text.ui.convertToText

/**
 * @author Thalys
 */
@ReadOnlyComposable
@Composable
fun UiText.toText(): String = convertToText(
    initialUiText = this,
    getString = { resId, args ->
        if (args == null) stringResource(resId)
        else stringResource(resId, *args)
    },
    getQuantityString = { resId, count, args ->
        if (args == null) pluralStringResource(resId, count)
        else pluralStringResource(resId, count, *args)
    }
)