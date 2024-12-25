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

import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.faker.genNonNegativeLong
import com.nlab.reminder.core.kotlin.isFailure
import com.nlab.reminder.core.kotlin.isSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

/**
 * @author Doohyun
 */
class TagEditStateFlowsKtTest {
    @Test
    fun `Given usageCount and intro, When updateIfIntro, Then stateFlow return success and changed with transform block`() {
        val usageCount = genNonNegativeLong()
        val initState = genIntroState()
        val expectedState = genTagEditStateExcludeTypeOf<TagEditState.Intro>()
        val stateFlow = MutableStateFlow<TagEditState?>(initState)

        val actualResult = stateFlow.updateIfIntro(
            getUsageCount = { Result.Success(usageCount) },
            transform = { _, _ -> expectedState }
        )

        assert(actualResult.isSuccess)
        assertThat(stateFlow.value, equalTo(expectedState))
    }

    @Test
    fun `Given 1000ms delayed usageCount and intro, When updateIfIntro and change state after 500ms, Then stateFlow return success and never changed to intro`() = runTest {
        val usageCount = genNonNegativeLong()
        val initState = genIntroState()
        val stateFlow = MutableStateFlow<TagEditState?>(initState)
        val expectedState = genRenameState()
        val tryState = genDeleteState()
        launch {
            delay(500)
            stateFlow.value = expectedState
        }
        val actualResult = stateFlow.updateIfIntro(
            getUsageCount = {
                delay(1000)
                Result.Success(usageCount)
            },
            transform = { _, _ -> tryState }
        )

        assert(actualResult.isSuccess)
        assertThat(stateFlow.value, equalTo(expectedState))
    }

    @Test
    fun `Given failed usageCount result and intro, When updateIfIntro, Then stateFlow return failure`() {
        val initState = genIntroState()
        val stateFlow = MutableStateFlow<TagEditState?>(initState)
        val actualResult = stateFlow.updateIfIntro(
            getUsageCount = { Result.Failure(RuntimeException()) },
            transform = { _, _ -> genTagEditStateExcludeTypeOf<TagEditState.Intro>() }
        )

        assert(actualResult.isFailure)
    }

    @Test
    fun `Given not intro, When updateIfIntro, Then stateFlow return success and never changed`() = runTest {
        val initState = genTagEditStateExcludeTypeOf<TagEditState.Intro>()
        val stateFlow = MutableStateFlow<TagEditState?>(initState)
        val actualResult = stateFlow.updateIfIntro(
            getUsageCount = { mock() },
            transform = { _, _ -> mock() }
        )

        assert(actualResult.isSuccess)
        assertThat(stateFlow.value, equalTo(initState))
    }

    @Test
    fun `Given processing with rename, When updateIfProcessingStateEquals with same target, Then stateFlow changed to expected state`() {
        val rename = genRenameState()
        val initState = TagEditState.Processing(rename)
        val stateFlow = MutableStateFlow<TagEditState?>(initState)

        val expectedState = genTagEditState()

        stateFlow.updateIfProcessingStateEquals(target = rename, to = expectedState)
        assertThat(stateFlow.value, equalTo(expectedState))
    }

    @Test
    fun `Given processing with rename, When updateIfProcessingStateEquals with another target, Then stateFlow never changed`() {
        val initState = TagEditState.Processing(genRenameState())
        val stateFlow = MutableStateFlow<TagEditState?>(initState)

        stateFlow.updateIfProcessingStateEquals(target = genDeleteState(), to = genTagEditState())
        assertThat(stateFlow.value, equalTo(initState))
    }

    @Test
    fun `Given not processing, When updateIfProcessingStateEquals, Then stateFlow never changed`() {
        val initState = genTagEditStateExcludeTypeOf<TagEditState.Processing>()
        val stateFlow = MutableStateFlow<TagEditState?>(initState)

        stateFlow.updateIfProcessingStateEquals(target = genRenameState(), to = genTagEditState())
        assertThat(stateFlow.value, equalTo(initState))
    }
}