/*
 * Copyright (C) 2023 The N's lab Open Source Project
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

package com.nlab.reminder.core.kotlin.collection.immutable

import com.nlab.testkit.genInt
import kotlinx.collections.immutable.toImmutableList
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author thalys
 */
internal class ExtensionsKtTest {
    @Test
    fun testMapToPersistentList() {
        val items = List(genInt(min = 10, max = 50)) { it }
        assertThat(
            items.mapToPersistentList { it.toString() },
            equalTo(items.map { it.toString() }.toImmutableList())
        )
    }
}