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

import com.nlab.reminder.core.component.tag.edit.TagEditState
import com.nlab.reminder.core.foundation.annotation.ExcludeFromGeneratedTestReport

/**
 * @author thalys
 */
internal sealed class HomeInteraction private constructor() {
    @ExcludeFromGeneratedTestReport
    data object Empty : HomeInteraction()

    @ExcludeFromGeneratedTestReport
    data object TodaySchedule : HomeInteraction()

    @ExcludeFromGeneratedTestReport
    data object TimetableSchedule : HomeInteraction()

    @ExcludeFromGeneratedTestReport
    data object AllSchedule : HomeInteraction()

    @ExcludeFromGeneratedTestReport
    data class TagEdit(val state: TagEditState) : HomeInteraction()
}