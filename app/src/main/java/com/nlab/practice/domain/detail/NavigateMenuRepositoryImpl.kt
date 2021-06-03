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

package com.nlab.practice.domain.detail

import com.nlab.practice.R
import com.nlab.practice.domain.home.NavigateMenu
import com.nlab.practice.domain.home.NavigateMenuRepository
import com.nlab.practice.domain.sample2.Sample2Destination
import kotlinx.coroutines.delay

/**
 * @author Doohyun
 */
class NavigateMenuRepositoryImpl : NavigateMenuRepository {

    override suspend fun getNavigateMenus(): List<NavigateMenu> {
        delay(1000)
        return listOf(NavigateMenu(R.string.home_title_1, R.string.home_description_1, R.color.purple_200, Sample2Destination()))
    }

}