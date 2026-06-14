/*
 * Copyright (C) 2026 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.schedulelist.content

import com.nlab.reminder.core.kotlin.collections.toSet
import dagger.Reusable
import javax.inject.Inject

/**
 * @author Doohyun
 */
@Reusable
class IsScheduleListResourceChangedUseCase @Inject constructor() {
    operator fun invoke(
        oldElements: Collection<ScheduleListElement>,
        newElements: Collection<ScheduleListElement>
    ): Boolean {
        val oldSchedules = oldElements
            .filterIsInstance<UserScheduleListResource>()
            .toSet { it.schedule }
        val newSchedules = newElements
            .filterIsInstance<UserScheduleListResource>()
            .toSet { it.schedule }
        return oldSchedules != newSchedules
    }
}
