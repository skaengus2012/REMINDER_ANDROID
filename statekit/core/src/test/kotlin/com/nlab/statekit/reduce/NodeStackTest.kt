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
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.trueValue
import org.junit.Test


/**
 * @author Doohyun
 */
class NodeStackTest {
    @Test
    fun `When created, Then has not ready state`() {
        assertThat(NodeStack<String>().isReady, not(trueValue()))
    }

    @Test
    fun `Given nodeStack, When ready, Then has ready state`() {
        val nodeStack = NodeStack<String>()
        nodeStack.ready()
        assertThat(nodeStack.isReady, trueValue())
    }

    @Test
    fun `Given used nodeStack, When released, Then has not ready state and empty data`() {
        val nodeStack = NodeStack<Int>()
        nodeStack.ready()
        nodeStack.add(genInt())
        nodeStack.release()

        assertThat(nodeStack.isReady, not(trueValue()))
        assertThat(nodeStack.removeLastOrNull(), nullValue())
    }

    @Test
    fun `Given data, When add and removeLastOrNull, Then return inputted data`() {
        val value = genBothify()
        val nodeStack = NodeStack<String>()
        nodeStack.add(value)
        assertThat(nodeStack.removeLastOrNull(), equalTo(value))
    }

    @Test
    fun `Given no data, When removeLastOrNull, Then return null`() {
        val data = NodeStack<Long>().removeLastOrNull()
        assert(data == null)
    }

    @Test
    fun `Given data, When removeLast, Then return inputted data`() {
        val value = genBoolean()
        val nodeStack = NodeStack<Boolean>()
        nodeStack.add(value)
        assertThat(nodeStack.removeLast(), equalTo(value))
    }

    @Test(expected = NoSuchElementException::class)
    fun `Given no data, When removeLast, Then throw NoSuchElementException`() {
        NodeStack<Long>().removeLast()
    }
}