/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.core.text

import com.nlab.testkit.faker.genInt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class JoinKtTest {
    @Test
    fun `Given number, When joining to uiText with separator, Then return correct value`() {
        val number = genInt()
        val expected = UiText(value = number.toString())
        val actual = listOf(number).joinToUiText(separatorRes = genInt())
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `Given numbers, When joining to uiText with separator, Then return correct value`() {
        val numbers = List(size = 3) { it }
        val separatorRes = genInt()
        val expected = UiText(
            resId = separatorRes,
            firstArg = UiText(value = numbers[0].toString()),
            secondArg = UiText(
                resId = separatorRes,
                firstArg = UiText(value = numbers[1].toString()),
                secondArg = UiText(value = numbers[2].toString())
            )
        )
        val actual = numbers.joinToUiText(separatorRes = separatorRes)
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `Given numbers, When joining to uiText with separator and transform, Then return correct value`() {
        val separatorRes = genInt()
        val decoratorRes = genInt()
        val numbers = List(size = 3) { it }
        val transform: (Int) -> UiText = { UiText(resId = decoratorRes, it.toString()) }
        val expected = UiText(
            resId = separatorRes,
            firstArg = transform(numbers[0]),
            secondArg = UiText(
                resId = separatorRes,
                firstArg = transform(numbers[1]),
                secondArg = transform(numbers[2])
            )
        )
        val actual = numbers.joinToUiText(separatorRes = separatorRes, transform)
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `When joining empty list, Then return correct value`() {
        val expected = EmptyUiText()
        val actual = emptyList<Any>().joinToUiText(separatorRes = genInt())
        assertThat(actual, equalTo(expected))
    }
}