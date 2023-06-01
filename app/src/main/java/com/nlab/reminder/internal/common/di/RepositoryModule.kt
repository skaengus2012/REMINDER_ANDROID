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

package com.nlab.reminder.internal.common.di

import com.nlab.reminder.core.kotlin.util.Result
import com.nlab.reminder.core.kotlin.util.onFailure
import com.nlab.reminder.domain.common.util.link.LinkMetadata
import com.nlab.reminder.domain.common.util.link.LinkMetadataRepository
import com.nlab.reminder.domain.common.schedule.ScheduleRepository as LegacyScheduleRepository
import com.nlab.reminder.domain.common.data.repository.*
import com.nlab.reminder.domain.common.util.link.LinkMetadataTableRepository
import com.nlab.reminder.internal.common.android.database.ScheduleDao
import com.nlab.reminder.internal.common.schedule.impl.LocalScheduleRepository
import com.nlab.reminder.internal.common.data.repository.LocalTagRepository
import com.nlab.reminder.domain.common.util.link.infra.JsoupLinkMetadataRepository
import com.nlab.reminder.internal.common.android.database.LinkMetadataDao
import com.nlab.reminder.internal.common.data.repository.FakeScheduleRepository
import com.nlab.reminder.internal.common.util.link.impl.LocalCachedLinkMetadataTableRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import java.util.Calendar
import javax.inject.Singleton

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Reusable
    @Binds
    abstract fun bindScheduleRepository(scheduleRepository: FakeScheduleRepository): ScheduleRepository

    @Reusable
    @Binds
    abstract fun bindTagRepository(tagRepository: LocalTagRepository): TagRepository

    companion object {
        @Reusable
        @Provides
        fun provideScheduleRepository(
            scheduleDao: ScheduleDao
        ): LegacyScheduleRepository = LocalScheduleRepository(scheduleDao)

        @Singleton
        @Provides
        fun provideLinkMetadataTableRepository(
            linkMetadataDao: LinkMetadataDao,
            @Singleton coroutineScope: CoroutineScope
        ): LinkMetadataTableRepository = LocalCachedLinkMetadataTableRepository(
            linkMetadataRepository = object : LinkMetadataRepository {
                private val internalRepository = JsoupLinkMetadataRepository()
                override suspend fun get(link: String): Result<LinkMetadata> =
                    internalRepository.get(link).onFailure { e -> Timber.w(e, "LinkThumbnail load failed.") }
            },
            linkMetadataDao,
            coroutineScope,
            getTimestamp = { Calendar.getInstance().timeInMillis }
        )
    }
}