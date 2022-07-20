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

package com.nlab.reminder.internal.feature.home

import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.tag.TagRepository
import com.nlab.reminder.domain.common.tag.genTag
import com.nlab.reminder.domain.feature.home.GetTagUsageCountUseCase
import com.nlab.reminder.test.genLong
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultGetTagUsageCountUseCaseTest {
    @Test
    fun `tagRepository found usageCount when useCase invoked`() = runTest {
        val tag: Tag = genTag()
        val expectedUsageCount: Long = genLong()
        val tagRepository: TagRepository = mock {
            whenever(mock.getUsageCount(tag)) doReturn expectedUsageCount
        }
        val getTagUsageCountUseCase: GetTagUsageCountUseCase = DefaultGetTagUsageCountUseCase(tagRepository)
        assertThat(getTagUsageCountUseCase(tag), equalTo(expectedUsageCount))
        verify(tagRepository, times(1)).getUsageCount(tag)
    }
}