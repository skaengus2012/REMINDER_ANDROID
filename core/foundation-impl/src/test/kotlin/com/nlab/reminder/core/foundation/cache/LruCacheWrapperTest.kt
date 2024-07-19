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

package com.nlab.reminder.core.foundation.cache

import androidx.collection.LruCache
import com.nlab.testkit.faker.genBothify
import com.nlab.testkit.faker.genInt
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @author Doohyun
 */
internal class LruCacheWrapperTest {
    @Test
    fun `Given k and v, When get, Then return v from converter`() {
        val k = genInt()
        val v = genBothify()
        val kvConverter: (Int) -> String = mock {
            whenever(mock(k)) doReturn v
        }
        val cache = LruCacheWrapperFactory().create(maxSize = 1, kvConverter)
        val actual = cache[k]
        assertThat(actual, equalTo(v))
    }

    @Test(expected = IllegalStateException::class)
    fun `When lruCache get null, Then post condition failed`() {
        val k = genInt()
        val lruCache: LruCache<Int, String> = mock {
            whenever(mock[k]) doReturn null
        }

        LruCacheWrapper(lruCache)[k]
    }

    /**
     * This is an integration test for LruCache that has nothing to do with coverage.
     * Because LruCache is implicit, if a cache exists, converter should not be called.
     */
    @Test
    fun `Given k and v, When get multiple times, Then converter called once`() = runTest {
        val k = genInt()
        val v = genBothify()
        val kvConverter: (Int) -> String = mock {
            whenever(mock(k)) doReturn v
        }
        val cache = LruCacheWrapperFactory().create(maxSize = 1, kvConverter)

        val testTimes = genInt(min = 2, max = 10)
        repeat(testTimes) { cache[k] }

        verify(kvConverter, once()).invoke(k)
    }
}