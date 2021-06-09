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
import com.nlab.practice2021.core.worker.DispatcherProvider
import com.nlab.practice2021.domain.home.model.NavigateMenu
import com.nlab.practice2021.domain.home.model.NavigateMenuRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

/**
 * @author Doohyun
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest : BaseTest() {

    private val dispatcherProvider: DispatcherProvider = mock<DispatcherProvider>().apply {
        whenever(io()).thenReturn(coroutineRule.testDispatcher)
    }

    @Test
    fun `When the viewModel is created, Expect to load data`() = coroutineRule.runBlockingTest {
        val navigateMenuRepository = spy(object : NavigateMenuRepository {
            override suspend fun getNavigateMenus(): List<NavigateMenu> {
                delay(1000)
                return emptyList()
            }
        })
        val viewModel = HomeViewModel(dispatcherProvider, navigateMenuRepository, HomeItemViewModel.Factory(mock()))

        with(viewModel.stateFlow.first()) {
            assertTrue(isLoading)
            assertFalse(isComplete)
        }
        verify(navigateMenuRepository, times(1)).getNavigateMenus()
        advanceTimeBy(1000)
        with(viewModel.stateFlow.first()) {
            assertFalse(isLoading)
            assertTrue(isComplete)
        }
    }

    @Test
    fun `When data loading is success, Expect factory will make items`() = coroutineRule.runBlockingTest {
        val dummyNavigateMenu = NavigateMenu(titleRes = 50, descriptionRes = 100, backgroundColorRes = 300, mock())
        val navigateMenuRepository = mock<NavigateMenuRepository>().apply {
            whenever(getNavigateMenus()).thenReturn(listOf(dummyNavigateMenu, dummyNavigateMenu))
        }
        val itemViewModelFactory = spy(HomeItemViewModel.Factory(mock()))
        val viewModel = HomeViewModel(dispatcherProvider, navigateMenuRepository, itemViewModelFactory)

        with(viewModel.stateFlow.first()) {
            assertEquals(2, items.size)
        }
        verify(itemViewModelFactory, times(2)).create(
            any(),
            any(),
            eq(dummyNavigateMenu.titleRes),
            eq(dummyNavigateMenu.descriptionRes),
            eq(dummyNavigateMenu.backgroundColorRes)
        )
    }

}