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

package com.nlab.reminder.core.network.di

import com.nlab.reminder.core.inject.qualifiers.coroutine.Dispatcher
import com.nlab.reminder.core.inject.qualifiers.coroutine.DispatcherOption.IO
import com.nlab.reminder.core.kotlin.NonBlankString
import com.nlab.reminder.core.kotlin.Result
import com.nlab.reminder.core.kotlin.onFailure
import com.nlab.reminder.core.kotlin.onSuccess
import com.nlab.reminder.core.network.LinkThumbnailDataSource
import com.nlab.reminder.core.network.LinkThumbnailDataSourceImpl
import com.nlab.reminder.core.network.LinkThumbnailResponse
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import timber.log.Timber

/**
 * @author Doohyun
 */
@Module
@InstallIn(SingletonComponent::class)
internal class NetworkModule {
    @Reusable
    @Provides
    fun provideLinkThumbnailDataSource(
        @Dispatcher(IO) dispatcher: CoroutineDispatcher
    ): LinkThumbnailDataSource = object : LinkThumbnailDataSource {
        private val linkThumbnailDataSource = LinkThumbnailDataSourceImpl(dispatcher)
        override suspend fun getLinkThumbnail(
            url: NonBlankString
        ): Result<LinkThumbnailResponse> = linkThumbnailDataSource.getLinkThumbnail(url)
            .onSuccess { response -> Timber.d("The linkMetadata loading success -> [$url : $response]") }
            .onFailure { e -> Timber.w(e, "The linkMetadata loading failed -> [$url]") }
    }
}