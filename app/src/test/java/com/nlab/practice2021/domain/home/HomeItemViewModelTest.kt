/*
 * Copyright (C) 2018 The N's lab Open Source Project
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
 *
 */

package com.nlab.practice2021.domain.home

import com.nlab.practice2021.BaseTest
import com.nlab.practice2021.core.effect.system.Destination
import com.nlab.practice2021.core.effect.system.SystemEffect
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeItemViewModelTest : BaseTest() {

    @Test
    fun `When viewmodel state subscribed, Expect to get correct state`() = coroutineRule.runBlockingTest {
        val factory = HomeItemViewModel.Factory(mock())
        val titleResource = 50
        val labelResource = 100
        val backgroundResource = 150

        with(
            factory.create(mock(), mock(), titleResource, labelResource, backgroundResource)
                .stateFlow
                .first()
        ) {
            assertEquals(titleResource, this.titleResource)
            assertEquals(labelResource, this.descriptionResource)
            assertEquals(backgroundResource, this.backgroundResource)
        }

        with(
            factory.create(mock(), mock(), titleResource, labelResource, backgroundResource)
                .stateFlow
                .first()
        ) {
            assertEquals(titleResource, this.titleResource)
            assertEquals(labelResource, this.descriptionResource)
            assertEquals(backgroundResource, this.backgroundResource)
        }

    }

    @Test
    fun `When viewmodel clicked by state, Expect to invoke navigation`() = coroutineRule.runBlockingTest {
        val destination = mock<Destination>()
        val systemEffect = mock<SystemEffect>()

        HomeItemViewModel.Factory(systemEffect)
            .create(coroutineRule, destination, 0, 0, 0)
            .stateFlow
            .first()
            .onClickItem()
        verify(systemEffect, times(1)).navigateTo(destination)
    }

}