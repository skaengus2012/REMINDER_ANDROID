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

import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * @author Doohyun
 */
class TagEditStateFlowsKtTest {
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