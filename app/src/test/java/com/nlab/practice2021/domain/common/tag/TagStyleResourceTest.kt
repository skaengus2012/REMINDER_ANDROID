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

package com.nlab.practice2021.domain.common.tag

import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Test

/**
 * @author Doohyun
 */
class TagStyleResourceTest {
    @Test
    fun `found tag when correct code inputted`() {
        listOf(1, 2, 3, 4, 5, 6)
        assertThat(
            (1..6).map { code -> TagStyleResource.findByCode(code) },
            equalTo(
                listOf(
                    TagStyleResource.TYPE1,
                    TagStyleResource.TYPE2,
                    TagStyleResource.TYPE3,
                    TagStyleResource.TYPE4,
                    TagStyleResource.TYPE5,
                    TagStyleResource.TYPE6
                )
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `thrown exception when invalid code inputted`() {
        TagStyleResource.findByCode(0)
    }

}