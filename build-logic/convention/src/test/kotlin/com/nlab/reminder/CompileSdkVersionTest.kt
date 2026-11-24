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

package com.nlab.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * @author Doohyun
 */
class CompileSdkVersionTest {
    @Test
    fun parseMajorOnly() {
        assertEquals(CompileSdkVersion(major = 37, minor = null), parseCompileSdkVersion("37"))
    }

    @Test
    fun parseMajorAndMinor() {
        assertEquals(CompileSdkVersion(major = 37, minor = 1), parseCompileSdkVersion("37.1"))
    }

    @Test
    fun parseInvalidFormats() {
        listOf("37.x", "37.1.2", "", "x", "37.", ".1", "-37").forEach { raw ->
            assertThrows(IllegalArgumentException::class.java) { parseCompileSdkVersion(raw) }
        }
    }
}
