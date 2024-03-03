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
package com.nlab.reminder.core.state

import androidx.annotation.StringRes
import com.nlab.reminder.core.annotation.test.ExcludeFromGeneratedTestReport
import com.nlab.reminder.core.annotation.platform.Stable
import kotlinx.collections.immutable.*

/**
 * @author Doohyun
 */
@Stable
sealed interface UserMessage {
    @ExcludeFromGeneratedTestReport
    @JvmInline
    value class ResIdValue(@StringRes val value: Int) : UserMessage
}

fun UserMessage(@StringRes value: Int): UserMessage = UserMessage.ResIdValue(value)

fun PersistentList<UserMessage>.userMessageShown(
    shownMessage: UserMessage
): PersistentList<UserMessage> = this - shownMessage