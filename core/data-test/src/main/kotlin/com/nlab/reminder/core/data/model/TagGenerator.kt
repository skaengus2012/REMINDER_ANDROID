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

import com.nlab.testkit.faker.genBothify
import com.nlab.testkit.faker.genInt
import com.nlab.testkit.faker.genLongGreaterThanZero

/**
 * @author Doohyun
 */
fun genTagId(value: Long = genLongGreaterThanZero()) = TagId.Present(value)

fun genTag(id: TagId = genTagId(), name: String = genBothify()) = Tag(id, name)

fun genTags(count: Int = genInt(min = 5, max = 10)): List<Tag> = List(count) { index ->
    val rawTagId = index.toLong() + 1
    genTag(genTagId(rawTagId), name = "Tag-$rawTagId")
}