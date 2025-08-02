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

package com.nlab.statekit.reduce

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class NodeStackKtTest {
    @Test
    fun `Given ordered number list, When collect numbers using addAllReversed, Then nodeStack has reversed list`() {
        val orderedList = listOf(1, 2, 3, 4, 5)
        val nodeStack = NodeStack<Int>()
        nodeStack.addAllReversed(orderedList)

        val actual = buildList {
            while (true) {
                val element = nodeStack.removeLastOrNull()
                if (element == null) break
                else add(element)
            }
        }
        assertThat(actual, equalTo(orderedList))
    }
}