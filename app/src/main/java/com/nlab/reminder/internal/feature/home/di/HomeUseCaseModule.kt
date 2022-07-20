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

package com.nlab.reminder.internal.feature.home.di

import com.nlab.reminder.domain.common.tag.TagRepository
import com.nlab.reminder.domain.feature.home.DeleteTagUseCase
import com.nlab.reminder.domain.feature.home.GetTagUsageCountUseCase
import com.nlab.reminder.internal.feature.home.DefaultDeleteTagUseCase
import com.nlab.reminder.internal.feature.home.DefaultGetTagUsageCountUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

/**
 * @author Doohyun
 */
@Module
@InstallIn(ViewModelComponent::class)
class HomeUseCaseModule {
    @Provides
    fun provideGetTagUsageCountUseCase(
        tagRepository: TagRepository
    ): GetTagUsageCountUseCase = DefaultGetTagUsageCountUseCase(tagRepository)

    @Provides
    fun provideDeleteTagUseCase(
        tagRepository: TagRepository
    ): DeleteTagUseCase = DefaultDeleteTagUseCase(tagRepository)
}