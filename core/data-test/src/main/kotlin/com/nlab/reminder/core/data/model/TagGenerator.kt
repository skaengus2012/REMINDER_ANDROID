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

import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.collections.toSet
import com.nlab.reminder.core.kotlin.faker.genNonBlankString
import com.nlab.reminder.core.kotlin.toNonBlankString
import com.nlab.testkit.faker.genInt
import com.nlab.testkit.faker.genLong

/**
 * @author Doohyun
 */
fun genTag(id: TagId = genTagId(), name: NonBlankString = genNonBlankString()) = Tag(id, name)

fun genTags(count: Int = genInt(min = 5, max = 10)): Set<Tag> = (1..count).toSet {
    Tag(
        id = TagId(it.toLong()),
        name = "Tag-${it.toLong()}".toNonBlankString()
    )
}

fun genTagId(): TagId = TagId(rawId = genLong())

fun genTagIds(count: Int = genInt(min = 5, max = 10)): Set<TagId> = genTags(count).toSet { it.id }