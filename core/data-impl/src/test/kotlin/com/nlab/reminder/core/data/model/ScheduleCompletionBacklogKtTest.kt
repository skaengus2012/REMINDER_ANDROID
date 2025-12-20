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

package com.nlab.reminder.core.data.model

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test

/**
 * @author Doohyun
 */
class ScheduleCompletionBacklogKtTest {
    @Test
    fun `Given entity, When convert to scheduleCompletionBacklog, Then return matching model`() {
        val (expectedBacklog, entity) = genScheduleCompletionBacklogAndEntity()
        val actualBacklog = ScheduleCompletionBacklog(entity)
        assertThat(actualBacklog, equalTo(expectedBacklog))
    }
}