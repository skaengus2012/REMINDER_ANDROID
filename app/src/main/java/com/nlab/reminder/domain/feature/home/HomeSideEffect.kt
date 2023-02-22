/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

import com.nlab.reminder.core.effect.SideEffect
import com.nlab.reminder.core.util.test.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.domain.common.tag.Tag

/**
 * @author thalys
 */
sealed class HomeSideEffect : SideEffect {
    object NavigateToday : HomeSideEffect()
    object NavigateTimetable : HomeSideEffect()
    object NavigateAllSchedule : HomeSideEffect()
    object ShowErrorPopup : HomeSideEffect()
    @ExcludeFromGeneratedTestReport data class NavigateTag(val tag: Tag) : HomeSideEffect()
    @ExcludeFromGeneratedTestReport data class ShowTagConfigPopup(val tag: Tag) : HomeSideEffect()
    @ExcludeFromGeneratedTestReport data class ShowTagRenamePopup(val tag: Tag, val usageCount: Long) : HomeSideEffect()
    @ExcludeFromGeneratedTestReport data class ShowTagDeletePopup(val tag: Tag, val usageCount: Long) : HomeSideEffect()
}