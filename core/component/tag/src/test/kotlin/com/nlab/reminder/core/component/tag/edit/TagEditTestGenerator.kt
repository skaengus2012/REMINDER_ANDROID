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

import com.nlab.reminder.core.data.model.Tag
import com.nlab.reminder.core.data.model.genTag
import com.nlab.reminder.core.kotlin.NonNegativeLong
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import com.nlab.testkit.faker.genBoolean
import com.nlab.testkit.faker.genBothify
import com.nlab.testkit.faker.genInt
import com.nlab.testkit.faker.requireSample
import com.nlab.testkit.faker.requireSampleExcludeTypeOf

/**
 * @author Doohyun
 */
private val sampleTagEditStates: List<TagEditState>
    get() = listOf(
        TagEditState.Empty,
        genIntroState(),
        genRenameState(),
        genMergeState(),
        genDeleteState(),
        TagEditState.Processing(
            when (genInt(min = 0, max = 2)) {
                0 -> genRenameState()
                1 -> genMergeState()
                else -> genDeleteState()
            }
        )
    )

fun genTagEditState(): TagEditState =
    sampleTagEditStates.requireSample()

internal inline fun <reified T : TagEditState> genTagEditStateExcludeTypeOf(): TagEditState =
    sampleTagEditStates.requireSampleExcludeTypeOf(listOf(T::class))

fun genIntroState() = TagEditState.Intro(tag = genTag(), usageCount = genNonNegativeLong())

fun genRenameState(
    tag: Tag = genTag(),
    usageCount: NonNegativeLong = genNonNegativeLong(),
    renameText: String = genBothify(),
    shouldUserInputReady: Boolean = genBoolean(),
) = TagEditState.Rename(tag, usageCount, renameText, shouldUserInputReady)

fun genMergeState(
    from: Tag = genTag(),
    to: Tag = genTag(),
) = TagEditState.Merge(from, to)

fun genDeleteState(
    tag: Tag = genTag(),
    usageCount: NonNegativeLong = genNonNegativeLong(),
) = TagEditState.Delete(tag, usageCount)