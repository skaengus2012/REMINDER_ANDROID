/*
 * Copyright (C) 2025 The N's lab Open Source Project
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

package com.nlab.reminder.core.component.schedulelist.di

import com.nlab.reminder.core.component.schedulelist.content.GetScheduleListResourcesFlowUseCase
import com.nlab.reminder.core.data.repository.LinkMetadataRepository
import com.nlab.reminder.core.data.repository.TagRepository
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * @author Thalys
 */
@Module
@InstallIn(SingletonComponent::class)
internal object AppScopeScheduleListModule {
    @Reusable
    @Provides
    fun provideGetScheduleListResourcesFlowUseCase(
        tagRepository: TagRepository,
        linkMetadataRepository: LinkMetadataRepository
    ) = GetScheduleListResourcesFlowUseCase(
        tagRepository = tagRepository,
        linkMetadataRepository = linkMetadataRepository
    )
}