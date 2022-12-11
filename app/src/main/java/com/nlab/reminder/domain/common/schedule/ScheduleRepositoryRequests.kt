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
package com.nlab.reminder.domain.common.schedule

import com.nlab.reminder.core.util.test.annotation.Generated

/**
 * @author thalys
 */
sealed class GetRequest private constructor() {
    object All : GetRequest()
    @Generated
    data class ByComplete(val isComplete: Boolean) : GetRequest()
}

sealed class UpdateRequest private constructor() {
    @Generated
    data class Completes(val values: List<ModifyCompleteRequest>) : UpdateRequest()
    @Generated
    data class VisiblePriorities(val values: List<ModifyVisiblePriorityRequest>) : UpdateRequest()
}

sealed class DeleteRequest private constructor() {
    @Generated
    data class ById(val scheduleId: ScheduleId) : DeleteRequest()
    @Generated
    data class ByComplete(val isComplete: Boolean) : DeleteRequest()
}

@Generated
data class ModifyCompleteRequest(val scheduleId: ScheduleId, val isComplete: Boolean)
@Generated
data class ModifyVisiblePriorityRequest(val scheduleId: ScheduleId, val visiblePriority: Long)