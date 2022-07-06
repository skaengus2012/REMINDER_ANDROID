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

package com.nlab.reminder.core.entrypoint.util

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
class EntryBlockTest {
    @Test
    fun testInvoke() {
        val block: () -> Unit = mock()
        val entryBlock = EntryBlock(block)
        entryBlock.invoke()

        verify(block, times(1))()
    }

    @Test
    fun testConcat() {
        val block1: () -> Unit = mock()
        val block2: () -> Unit = mock()

        listOf(EntryBlock { block1() }, EntryBlock { block2() })
            .concat()
            .invoke()
        verify(block1, times(1))()
        verify(block2, times(1))()
    }

    @Test
    fun testPlus() {
        val block1: () -> Unit = mock()
        val block2: () -> Unit = mock()
        val entryBlock = EntryBlock { block1() } + EntryBlock { block2() }
        entryBlock.invoke()
        verify(block1, times(1))()
        verify(block2, times(1))()
    }
}