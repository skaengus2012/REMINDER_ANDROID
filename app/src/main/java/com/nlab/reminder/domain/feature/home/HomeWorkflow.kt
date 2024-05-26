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

package com.nlab.reminder.domain.feature.home

import com.nlab.reminder.core.annotation.platform.Stable
import com.nlab.reminder.core.annotation.test.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.TagUsageCount

/**
 * @author thalys
 */
@Stable
internal sealed interface HomeWorkflow {
    data object Empty : HomeWorkflow
    data object TodaySchedule : HomeWorkflow
    data object TimetableSchedule : HomeWorkflow
    data object AllSchedule : HomeWorkflow

    @ExcludeFromGeneratedTestReport
    data class TagConfig(
        val tag: Tag,
        val usageCount: TagUsageCount
    ) : HomeWorkflow

    @ExcludeFromGeneratedTestReport
    data class TagRename(
        val tag: Tag,
        val usageCount: TagUsageCount,
        val renameText: String,
        val shouldKeyboardShown: Boolean
    ) : HomeWorkflow

    @ExcludeFromGeneratedTestReport
    data class TagDelete(
        val tag: Tag,
        val usageCount: TagUsageCount
    ) : HomeWorkflow
}