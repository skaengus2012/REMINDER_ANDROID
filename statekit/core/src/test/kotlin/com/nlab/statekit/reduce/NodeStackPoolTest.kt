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

import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class NodeStackPoolTest {
    @Test
    fun `When request after release, Then return same instance`() {
        val pool = NodeStackPool()

        val firstTimePool = pool.request<Int>()
        pool.release(firstTimePool)

        val secondTimePool = pool.request<String>()
        assertThat(secondTimePool, sameInstance(firstTimePool))
    }

    @Test
    fun `When request while pool acc used all, Then pool make new instance`() {
        val pool = NodeStackPool()

        val firstTimePool = pool.request<Int>()
        val secondTimePool = pool.request<Int>()

        assertThat(secondTimePool, not(sameInstance(firstTimePool)))
    }
}