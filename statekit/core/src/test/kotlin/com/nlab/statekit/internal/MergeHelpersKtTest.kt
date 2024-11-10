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

package com.nlab.statekit.internal

import com.nlab.testkit.faker.genInt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Thalys
 */
class MergeHelpersKtTest {
    @Test
    fun `Given empty list, When merge, Then return null`() {
        val target = emptyList<Any>()
        val ret = target.merge { _, _ -> error("Should not be called") }
        assertThat(ret, nullValue())
    }

    @Test
    fun `Given single list, When merge, Then return first element`() {
        val expected = Any()
        val target = listOf(expected)
        val actual = target.merge { _, _ -> error("Should not be called") }
        assertThat(actual, sameInstance(expected))
    }

    @Test
    fun `Given 2 or more list, When merge, Then return first element`() {
        val expectedSize = genInt(min = 2, max = 10)
        val expectedList = List(expectedSize) { 1 }
        val actual = expectedList.merge { head, tails ->
            head + tails.reduce { acc, i -> acc + i }
        }
        assertThat(actual, equalTo(expectedSize))
    }
}