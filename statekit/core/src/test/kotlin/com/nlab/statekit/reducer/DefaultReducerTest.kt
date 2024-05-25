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

package com.nlab.statekit.reducer

import com.nlab.statekit.*
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.once
import org.mockito.kotlin.verify

/**
 * @author thalys
 */
internal class DefaultReducerTest {
    @Test
    fun `When an UpdateSource is passed to the reduce of Reducer, it should invoke the provided mock`() {
        val mockAction = TestAction.genAction()
        val mockState = TestState.genState()
        val mockReducer: (UpdateSource<TestAction, TestState>) -> TestState = mock()
        val reducer = DefaultReducer(mockReducer)

        reducer(UpdateSource(mockAction, mockState))
        verify(mockReducer, once())(UpdateSource(mockAction, mockState))
    }
}