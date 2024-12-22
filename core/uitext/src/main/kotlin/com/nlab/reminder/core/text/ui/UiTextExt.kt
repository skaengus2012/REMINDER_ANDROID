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

package com.nlab.reminder.core.text.ui

import android.content.Context
import com.nlab.reminder.core.text.UiText

/**
 * @author Thalys
 */
fun UiText.toText(context: Context): String = when (this) {
    is UiText.Direct -> value
    is UiText.ResId -> {
        if (args == null) context.getString(resId)
        else context.getString(resId, *args)
    }

    is UiText.PluralsResId -> {
        if (args == null) context.resources.getQuantityString(resId, count)
        else context.resources.getQuantityString(resId, count, *args)
    }
}