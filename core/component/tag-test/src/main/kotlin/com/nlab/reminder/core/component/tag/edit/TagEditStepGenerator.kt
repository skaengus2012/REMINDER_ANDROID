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

package com.nlab.reminder.core.component.tag.edit

import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genBothify
import com.nlab.testkit.faker.genInt
import com.nlab.testkit.faker.requireSample
import com.nlab.testkit.faker.requireSampleExcludeTypeOf
import kotlin.reflect.KClass

/**
 * @author Thalys
 */
fun genTagEditStateOrNull(): TagEditState? = getNullableTagEditStates().requireSample()

fun genTagEditState(): TagEditState = getNullableTagEditStates().filterNotNull().requireSample()

fun genTagEditStateExcludeTypeOf(
    firstType: KClass<out TagEditState>,
    vararg anotherTypes: KClass<out TagEditState>
): TagEditState = getNullableTagEditStates().filterNotNull().requireSampleExcludeTypeOf(
    buildList {
        add(firstType)
        addAll(anotherTypes)
    }
)

inline fun <reified T : TagEditState> genTagEditStateExcludeTypeOf(): TagEditState =
    genTagEditStateExcludeTypeOf(T::class)

private fun getNullableTagEditStates(): List<TagEditState?> {
    val rename = TagEditState.Rename(
        tag = genTag(),
        usageCount = genNonNegativeLong(),
        renameText = genBothify(),
        shouldUserInputReady = genBoolean()
    )
    val merge = TagEditState.Merge(
        from = genTag(),
        to = genTag()
    )
    val delete = TagEditState.Delete(
        tag = genTag(),
        usageCount = genNonNegativeLong()
    )
    return listOf(
        null,
        TagEditState.Intro(
            tag = genTag(),
            usageCount = genNonNegativeLong()
        ),
        rename,
        merge,
        delete,
        TagEditState.Processing(
            when (genInt(min = 0, max = 2)) {
                0 -> rename
                1 -> merge
                else -> delete
            }
        )
    )
}