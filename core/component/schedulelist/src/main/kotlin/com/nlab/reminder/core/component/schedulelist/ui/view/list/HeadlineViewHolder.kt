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

package com.nlab.reminder.core.component.schedulelist.ui.view.list

import com.nlab.reminder.core.android.content.getThemeColor
import com.nlab.reminder.core.component.schedulelist.databinding.LayoutScheduleAdapterItemHeadlineBinding
import com.nlab.reminder.core.designsystem.compose.theme.AttrIds

/**
 * @author Doohyun
 */
class HeadlineViewHolder internal constructor(
    private val binding: LayoutScheduleAdapterItemHeadlineBinding,
    theme: ScheduleListTheme
) : ScheduleAdapterItemViewHolder(binding.root) {
    init {
        val textColorAttrRes = when (theme) {
            ScheduleListTheme.Point1 -> AttrIds.point_1
            ScheduleListTheme.Point2 -> AttrIds.point_2
            ScheduleListTheme.Point3 -> AttrIds.point_3
        }
        binding.textviewTitle.setTextColor(itemView.context.getThemeColor(textColorAttrRes))
    }

    fun bind(item: ScheduleAdapterItem.Headline) {
        binding.textviewTitle.setText(item.textRes)
    }
}