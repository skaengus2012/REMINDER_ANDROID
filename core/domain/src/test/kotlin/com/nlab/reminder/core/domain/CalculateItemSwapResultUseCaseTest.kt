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

package com.nlab.reminder.core.domain

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
internal class CalculateItemSwapResultUseCaseTest {
    @Test
    fun `Given same positions, Then returns emptyList`() {
        testWhenIllegalPositionsGetEmptyList(
            items = (0..10).toList(),
            fromPosition = 5,
            toPosition = 5
        )
    }

    @Test
    fun `Given illegal input positions, Then returns emptyList`() {
        testWhenIllegalPositionsGetEmptyList(
            items = (0..10).toList(),
            fromPosition = -1,
            toPosition = 5
        )

        testWhenIllegalPositionsGetEmptyList(
            items = (0..10).toList(),
            fromPosition = 11,
            toPosition = 5
        )

        testWhenIllegalPositionsGetEmptyList(
            items = (0..10).toList(),
            fromPosition = 5,
            toPosition = 20
        )

        testWhenIllegalPositionsGetEmptyList(
            items = (0..10).toList(),
            fromPosition = 5,
            toPosition = -1
        )
    }

    @Test
    fun `When item is moved down, Then returns movement result`() {
        val items = (0..10).toList()
        val result = CalculateItemSwapResultUseCase().invoke(items, fromPosition = 1, toPosition = 4)
        assertThat(result, equalTo(listOf(2, 3, 4, 1)))
    }

    @Test
    fun `When item is moved up, Then returns movement result`() {
        val items = (0..10).toList()
        val result = CalculateItemSwapResultUseCase().invoke(items, fromPosition = 6, toPosition = 1)
        assertThat(result, equalTo(listOf(6, 1, 2, 3, 4, 5)))
    }
}

private fun testWhenIllegalPositionsGetEmptyList(
    items: List<Int>,
    fromPosition: Int,
    toPosition: Int
) {
    val result = CalculateItemSwapResultUseCase().invoke(items, fromPosition, toPosition)
    assertThat(result, equalTo(emptyList()))
}