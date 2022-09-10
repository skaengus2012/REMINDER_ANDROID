/*
 * Copyright (C) 2022 The N's lab Open Source Project
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

package com.nlab.reminder.domain.feature.home

import com.nlab.reminder.core.state.StateController
import com.nlab.reminder.core.state.verifyStateSendExtension
import com.nlab.reminder.domain.common.tag.Tag
import com.nlab.reminder.domain.common.tag.genTag
import com.nlab.reminder.test.genBothify
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
class HomeViewModelsKtTest {
    @Test
    fun testExtensions() {
        val stateController: StateController<HomeEvent, HomeState> = mock()
        val tag: Tag = genTag()
        val randomString: String = genBothify()
        val viewModel = HomeViewModel(
            stateControllerFactory = mock {
                whenever(mock.create(any(), any())) doReturn stateController
            }
        )

        verifyStateSendExtension(
            stateController,
            HomeEvent.OnRetryClicked
        ) { viewModel.onRetryClicked() }

        verifyStateSendExtension(
            stateController,
            HomeEvent.OnTodayCategoryClicked
        ) { viewModel.onTodayCategoryClicked() }

        verifyStateSendExtension(
            stateController,
            HomeEvent.OnTimetableCategoryClicked
        ) { viewModel.onTimetableCategoryClicked() }

        verifyStateSendExtension(
            stateController,
            HomeEvent.OnAllCategoryClicked
        ) { viewModel.onAllCategoryClicked() }

        verifyStateSendExtension(
            stateController,
            HomeEvent.OnTagClicked(tag)
        ) { viewModel.onTagClicked(tag) }

        verifyStateSendExtension(
            stateController,
            HomeEvent.OnTagLongClicked(tag)
        ) { viewModel.onTagLongClicked(tag) }

        verifyStateSendExtension(
            stateController,
            HomeEvent.OnTagRenameRequestClicked(tag)
        ) { viewModel.onTagRenameRequestClicked(tag) }

        verifyStateSendExtension(
            stateController,
            HomeEvent.OnTagRenameConfirmClicked(tag, randomString)
        ) { viewModel.onTagRenameConfirmClicked(tag, randomString) }

        verifyStateSendExtension(
            stateController,
            HomeEvent.OnTagDeleteRequestClicked(tag)
        ) { viewModel.onTagDeleteRequestClicked(tag) }

        verifyStateSendExtension(
            stateController,
            HomeEvent.OnTagDeleteConfirmClicked(tag)
        ) { viewModel.onTagDeleteConfirmClicked(tag) }
    }
}