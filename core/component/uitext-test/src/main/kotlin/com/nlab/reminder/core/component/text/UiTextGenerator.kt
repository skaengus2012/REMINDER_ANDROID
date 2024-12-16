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

package com.nlab.reminder.core.component.text

import com.nlab.testkit.faker.genBothify
import com.nlab.testkit.faker.genInt

/**
 * @author Thalys
 */
fun genUiText(value: String = genBothify()): UiText = UiText(value)

fun genUiTexts(size: Int = genInt(min = 1, max = 10)): List<UiText> = List(size) {
    genUiText(value = "${genBothify()}_$it")
}