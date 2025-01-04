/*
 * Copyright (C) 2024 The N's lab Open Source Project
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

package com.nlab.reminder.core.data.repository.impl

import com.nlab.reminder.core.data.model.genScheduleId
import com.nlab.reminder.core.data.repository.getSnapshot
import com.nlab.testkit.faker.genBoolean
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
internal class InMemoryScheduleDetailCompleteMarkRepositoryTest {
    @Test
    fun `When added, Then updated completeMark table`() = runTest {
        val scheduleId = genScheduleId()
        val isComplete = genBoolean()
        val repository = InMemoryScheduleCompleteMarkRepository()

        repository.add(scheduleId, isComplete)
        assertThat(
            repository.getSnapshot().getValue(scheduleId),
            equalTo(isComplete)
        )
    }

    @Test
    fun `Given already added completeMark, When same scheduleId completion added, Then old completeMark override`() = runTest {
        val scheduleId = genScheduleId()
        val oldCompleteMark = genBoolean()
        val newCompleteMark = oldCompleteMark.not()
        val scheduleCompleteMarkRepository = InMemoryScheduleCompleteMarkRepository().apply {
            add(scheduleId, oldCompleteMark)
        }

        scheduleCompleteMarkRepository.add(scheduleId, newCompleteMark)
        assertThat(
            scheduleCompleteMarkRepository.getSnapshot(),
            equalTo(mapOf(scheduleId to newCompleteMark))
        )
    }
}