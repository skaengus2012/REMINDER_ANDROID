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
import com.nlab.reminder.core.util.link.LinkThumbnail
import com.nlab.reminder.core.util.link.LinkThumbnailRepository
import com.nlab.reminder.core.util.link.impl.CachedLinkThumbnailRepository
import com.nlab.reminder.domain.common.schedule.ScheduleRepository
import com.nlab.reminder.domain.common.tag.TagRepository
import com.nlab.reminder.internal.common.android.database.ScheduleDao
import com.nlab.reminder.internal.common.android.database.ScheduleTagListDao
import com.nlab.reminder.internal.common.android.database.TagDao
import com.nlab.reminder.internal.common.schedule.impl.LocalScheduleRepository
import com.nlab.reminder.internal.common.tag.impl.LocalTagRepository
import com.nlab.reminder.internal.util.link.impl.JsoupLinkThumbnailRepository
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Reusable
    @Provides
    fun provideScheduleRepository(
        scheduleDao: ScheduleDao
    ): ScheduleRepository = LocalScheduleRepository(scheduleDao)

    @Reusable
    @Provides
    fun provideTagRepository(
        tagDao: TagDao,
        scheduleTagListDao: ScheduleTagListDao
    ): TagRepository = LocalTagRepository(tagDao, scheduleTagListDao)

    @Reusable
    @Provides
    fun provideLinkThumbnailRepository(): LinkThumbnailRepository = object : LinkThumbnailRepository {
        private val internalRepository: LinkThumbnailRepository = CachedLinkThumbnailRepository(
            JsoupLinkThumbnailRepository()
        )

        override suspend fun get(link: String): Result<LinkThumbnail> {
            return internalRepository.get(link).onFailure { e -> Timber.w(e, "LinkThumbnail load failed.") }
        }
    }
}