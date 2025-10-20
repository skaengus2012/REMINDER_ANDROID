/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.schedulelist.content.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.nlab.reminder.core.component.schedulelist.content.UserScheduleListResource

/**
 * @author Doohyun
 */
@Immutable
sealed class ScheduleListItem {
    data class Add(
        val newScheduleSource: Any?,
        val line: AddLine
    ) : ScheduleListItem()

    data class Content(
        val schedule: UserScheduleListResource,
        val isLineVisible: Boolean
    ) : ScheduleListItem()

    data class FooterAdd(
        val newScheduleSource: Any? = null, // TODO implements
        val line: AddLine
    ) : ScheduleListItem()

    data class Headline(@StringRes val textRes: Int) : ScheduleListItem()

    data object HeadlinePadding : ScheduleListItem()

    data class GroupHeader(
        val title: String,
        val subTitle: CharSequence,
    ) : ScheduleListItem(), StickyHeaderItem

    data class SubGroupHeader(
        val title: CharSequence
    ) : ScheduleListItem(), StickyHeaderItem
}