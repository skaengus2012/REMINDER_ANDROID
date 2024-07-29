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

package com.nlab.reminder.core.data.model

import com.nlab.testkit.faker.genBlank
import com.nlab.testkit.faker.genBothify
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
internal class LinkTest {
    @Test(expected = IllegalArgumentException::class)
    fun `Given blank value, When created, Then precondition failed`() {
        Link.Present(value = genBlank())
    }

    @Test
    fun `Empty objects are always equivalent`() {
        val expected = Link.Empty
        val actual = Link.Empty
        assert(actual == expected)
    }

    @Test
    fun `Given value not blanked, When created, Then success`() {
        val given = genBothify()
        val link = Link.Present(given)
        assertThat(link.value, equalTo(given))
    }
}