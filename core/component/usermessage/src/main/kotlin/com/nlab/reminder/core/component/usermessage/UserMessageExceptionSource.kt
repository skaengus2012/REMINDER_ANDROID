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

package com.nlab.reminder.core.component.usermessage

import com.nlab.reminder.core.annotation.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.text.UiText

/**
 * @author Thalys
 */
// Since it is defined as an internal constructor, it is impossible to use data classes.
// Declaration is possible, but it is impossible to use functions such as copy.
@ExcludeFromGeneratedTestReport
class UserMessageExceptionSource internal constructor(
    val id: UserMessageId,
    val message: UiText?,
    val priority: FeedbackPriority?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserMessageExceptionSource

        if (id != other.id) return false
        if (message != other.message) return false
        if (priority != other.priority) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + (priority?.hashCode() ?: 0)
        return result
    }
}