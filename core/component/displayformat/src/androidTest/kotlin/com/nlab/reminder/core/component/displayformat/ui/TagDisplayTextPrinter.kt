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

package com.nlab.reminder.core.component.displayformat.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.kotlin.toNonBlankString
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @author Doohyun
 */
@RunWith(AndroidJUnit4::class)
class TagDisplayTextPrinter {
    @Test
    fun print() {
        val tag = genTag(name = "Hello".toNonBlankString())
        println("$tag is ${tagDisplayText(tag)}")
    }
}