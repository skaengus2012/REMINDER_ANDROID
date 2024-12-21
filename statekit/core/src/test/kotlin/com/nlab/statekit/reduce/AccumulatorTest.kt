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

import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genBothify
import com.nlab.testkit.faker.genInt
import com.nlab.testkit.faker.genLong
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


/**
 * @author Doohyun
 */
class AccumulatorTest {
    @Test
    fun `When created, Then has not ready state`() {
        assert(Accumulator<String>().isReady.not())
    }

    @Test
    fun `Given accumulator, When ready, Then has ready state`() {
        val acc = Accumulator<String>()
        acc.ready()
        assert(acc.isReady)
    }

    @Test
    fun `Given used accumulator, When released, Then has not ready state and empty data`() {
        val acc = Accumulator<Int>()
        acc.ready()
        acc.add(genInt())
        acc.release()

        assert(acc.isReady.not())
        assert(acc.removeLastOrNull() == null)
    }

    @Test
    fun `Given data, When add and removeLastOrNull, Then return inputted data`() {
        val value = genBothify()
        val acc = Accumulator<String>()
        acc.add(value)
        assertThat(acc.removeLastOrNull(), equalTo(value))
    }

    @Test
    fun `Given no data, When removeLastOrNull, Then return null`() {
        val data = Accumulator<Long>().removeLastOrNull()
        assert(data == null)
    }

    @Test
    fun `Given data, When removeLast, Then return inputted data`() {
        val value = genBoolean()
        val acc = Accumulator<Boolean>()
        acc.add(value)
        assertThat(acc.removeLast(), equalTo(value))
    }

    @Test(expected = NoSuchElementException::class)
    fun `Given no data, When removeLast, Then throw NoSuchElementException`() {
        Accumulator<Long>().removeLast()
    }
}