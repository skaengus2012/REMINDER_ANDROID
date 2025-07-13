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

package com.nlab.statekit.dsl.reduce

import com.nlab.statekit.reduce.NodeStackPool
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class NodeStackExtKtTest {
    @Test
    fun `Given ordered numbers, When addAllReversedWithoutHead, Then nodeStack has reversed list without first`() {
        val orderedList = listOf(1, 2, 3, 4, 5)
        val expectedList = orderedList.subList(1, orderedList.size)
        val nodeStack = NodeStackPool().request<Int>()
        nodeStack.addAllReversedWithoutHead(orderedList)

        val actual = buildList {
            while (true) {
                val element = nodeStack.removeLastOrNull()
                if (element == null) break
                else add(element)
            }
        }
        assertThat(actual, equalTo(expectedList))
    }
}