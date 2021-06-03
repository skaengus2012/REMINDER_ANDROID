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

package com.nlab.practice.domain.home

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nlab.practice.core.effect.system.SystemEffect
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.mockito.kotlin.doReturn

/**
 * @author Doohyun
 */
@Suppress("UNCHECKED_CAST", "TestFunctionName")
@RunWith(AndroidJUnit4::class)
class HomeViewModelTest {

    @Test
    fun When_ViewModelCreated_Expected_CallNavigateMenu() {
        ActivityScenario.launch(HomeActivity::class.java).use { scenario ->
            scenario.onActivity {
                val navigateMenuRepository = mock<NavigateMenuRepository>().apply {
                    stub {
                        onBlocking { getNavigateMenus() }.doReturn(emptyList())
                    }
                }
                val systemEffect = mock<SystemEffect>()

                HomeViewModel(navigateMenuRepository, HomeItemViewModel.Factory(systemEffect))
                verifyBlocking(navigateMenuRepository, times(1)) {
                    getNavigateMenus()
                }
            }
        }
    }
}