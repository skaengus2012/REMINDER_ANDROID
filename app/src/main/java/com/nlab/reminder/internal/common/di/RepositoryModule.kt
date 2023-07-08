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

package com.nlab.reminder.internal.common.di

import com.nlab.reminder.core.kotlin.util.*
import com.nlab.reminder.domain.common.data.model.*
import com.nlab.reminder.domain.common.data.repository.*
import com.nlab.reminder.domain.common.data.repository.infra.*
import com.nlab.reminder.internal.common.data.repository.*
import dagger.*
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

/**
 * @author thalys
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Reusable
    @Binds
    abstract fun bindScheduleRepository(scheduleRepository: LocalScheduleRepository): ScheduleRepository

    @Reusable
    @Binds
    abstract fun bindTagRepository(tagRepository: LocalTagRepository): TagRepository

    @Reusable
    @Binds
    abstract fun bindTimestampRepository(timestampRepository: DefaultTimestampRepository): TimestampRepository

    companion object {
        @Reusable
        @Provides
        fun provideScheduleRepository(): LinkMetadataRepository = object : LinkMetadataRepository {
            private val internalRepository = JsoupLinkMetadataRepository(dispatcher = Dispatchers.IO)
            override suspend fun get(link: Link): Result<LinkMetadata> {
                return internalRepository.get(link).onFailure {
                    Timber.w(it, "LinkMetadata load failed.")
                }
            }
        }
    }
}