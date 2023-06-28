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

import com.nlab.testkit.genInt
import kotlinx.collections.immutable.toPersistentList
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class UserMessageKtTest {
    @Test
    fun testUserMessageWithStringRes() {
        val resourceId = genInt(min = 0, max = 100)
        assertThat(
            UserMessage(resourceId),
            equalTo(UserMessage.ResIdValue(resourceId))
        )
    }

    @Test
    fun `Only one UserMessage is removed at a time from the beginning`() {
        val targetId: Int
        val expectedUserMessages =
            List(genInt(min = 5, max = 10)) { index -> UserMessage.ResIdValue(index) }
                .also { list -> targetId = list.first().value }
                .shuffled()
                .toPersistentList()
        assertThat(
            expectedUserMessages
                .add(0, UserMessage.ResIdValue(targetId))
                .userMessageShown(UserMessage.ResIdValue(targetId)),
            equalTo(expectedUserMessages)
        )
    }
}